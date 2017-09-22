package org.genesys.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.genesys.decide.Decider;
import org.genesys.models.Example;
import org.genesys.models.Pair;
import org.genesys.models.Problem;
import org.genesys.utils.LibUtils;

public class DeepCoderPythonDecider implements Decider {
	private static final String FILENAME = "./model/tmp/deep_coder.txt";
	private static final String FUNC_FILENAME = "./model/data/deep_coder_funcs.txt";
	private static final String PYTHON_PATH_FILENAME = "./model/tmp/python_path.txt";
	private static final String COMMAND;
	
	static {
		String pythonPath = "";
		if(new File(PYTHON_PATH_FILENAME).exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(PYTHON_PATH_FILENAME));
				pythonPath = br.readLine();
				br.close();
			} catch(IOException e) {}
		}
		COMMAND = pythonPath + "python -m model.genesys.run";
	}
	
	private final XFeaturizer<Object> xFeaturizer;
	private final YFeaturizer yFeaturizer;
	private final Object input;
	private final Object output;
	
	private final double[] probabilities;
	
	public static List<String> getDeepCoderFunctions() {
		List<String> functions = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(FUNC_FILENAME));
			String line;
			while((line = br.readLine()) != null) {
				functions.add(line);
			}
			br.close();
			return functions;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static DeepCoderInputSamplerParameters getDeepCoderParameters() {
		// parameters
		int minLength = 5;
		int maxLength = 20;
		int minValue = -256;
		int maxValue = 255;
		
		return new DeepCoderInputSamplerParameters(minLength, maxLength, maxValue, minValue);
	}

	public DeepCoderPythonDecider(Problem problem) {
		// parameters
		DeepCoderInputSamplerParameters inputSamplerParameters  = getDeepCoderParameters();
		
		// functions
		List<String> functions = getDeepCoderFunctions();
		
		// featurizers
		this.xFeaturizer = new DeepCoderXFeaturizer(inputSamplerParameters);
		this.yFeaturizer  = new YFeaturizer(functions);

		//FIXME: Osbert is assuming we only have one input from ONE example.
		Example example = problem.getExamples().get(0);
		input = LibUtils.fixGsonBug(example.getInput());
		// Always one output table
		output = LibUtils.fixGsonBug(example.getOutput());

		this.probabilities = this.build();
	}
	
	public DeepCoderPythonDecider(XFeaturizer<Object> xFeaturizer, YFeaturizer yFeaturizer, Object input, Object output) {
		this.xFeaturizer = xFeaturizer;
		this.yFeaturizer = yFeaturizer;
		this.input = input;
		this.output = output;
		
		this.probabilities = this.build();
	}
	
	public double getProbability(String function) {
		if(!this.hasProbability(function)) {
			return 0.0;
		} else {
			return this.probabilities[this.yFeaturizer.functionIndices.get(function)];
		}
	}
	
	public boolean hasProbability(String function) {
		return this.yFeaturizer.functionIndices.containsKey(function);
	}

	@Override
	public String decideSketch(List<String> trail, List<String> candidates, int child) { return decide(trail, candidates); }

	@Override
	public String decide(List<String> ancestors, List<String> functionChoices) {
		// get the most likely function
		String maxFunction = null;
		double maxProbability = -1.0;
		for(String function : functionChoices) {
			if(maxProbability <= this.getProbability(function)) {
				maxFunction = function;
				maxProbability = this.getProbability(function);
			}
		}
		
		if(maxFunction == null) {
			throw new RuntimeException();
		}
		
		return maxFunction;
	}
	
	private double[] build() {
		// Step 1: Build the test set
		try {
			File file = new File(FILENAME);
			if(!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			PrintWriter pw = new PrintWriter(new FileWriter(file));
			
			// Step 1a: Build the datapoint
			Pair<List<Integer>,List<Integer>> features = this.xFeaturizer.getFeatures(this.input, this.output);
			String datapoint = "(" + Utils.toString(features.t0) + ", " + Utils.toString(features.t1) + ")";
			
			// Step 1b: Print to test set file
			pw.println(datapoint);
			
			pw.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		// Step 2: Get the neural net output
		try {
			// Step 2a: Execute the Python script
			Process p = Runtime.getRuntime().exec(COMMAND);
			
			// Step 2b: Read the output
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			double[] probabilities = null;
			while((line = br.readLine()) != null) {
				if(line.startsWith("RESULT: ")) {
					String[] tokens = line.substring(9, line.length()-1).split(", ");
					probabilities = new double[tokens.length];
					for(int i=0; i<tokens.length; i++) {
						probabilities[i] = Double.parseDouble(tokens[i]);
					}
					if(probabilities.length != this.yFeaturizer.functions.size()) {
						throw new RuntimeException("Invalid number of probabilities!");
					}
				}
			}
			br.close();
			
			// Step 2c: Wait for the process to finish
			p.waitFor();
			
			// Step 2d: Make sure we read the results
			if(probabilities == null) {
				throw new RuntimeException();
			}
			
			return probabilities;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
