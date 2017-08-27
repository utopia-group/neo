package org.genesys.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genesys.decide.Decider;
import org.genesys.models.Trio;

public class DeepCoderPythonDecider implements Decider {
	private static final String FILENAME = "./model/tmp/deep_coder.txt";
	private static final String PYTHON_PATH_FILENAME = "./model/tmp/python_path.txt";
	private static final String COMMAND;
	private static final int N_GRAM_LENGTH = 2;
	
	static {
		String pythonPath = "";
		if(new File(PYTHON_PATH_FILENAME).exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(PYTHON_PATH_FILENAME));
				pythonPath = br.readLine();
				br.close();
			} catch(IOException e) {}
		}
		COMMAND = pythonPath + "python -m model.genesys.test";
	}
	
	private final XFeaturizer<Object> xFeaturizer;
	private final YFeaturizer yFeaturizer;
	private final Object input;
	private final Object output;
	
	private final Map<String,double[]> probabilities = new HashMap<String,double[]>();
	
	public DeepCoderPythonDecider(XFeaturizer<Object> xFeaturizer, YFeaturizer yFeaturizer, Object input, Object output) {
		this.xFeaturizer = xFeaturizer;
		this.yFeaturizer = yFeaturizer;
		this.input = input;
		this.output = output;
		
		this.build();
	}
	
	@Override
	public String decide(List<String> ancestors, List<String> functionChoices) {
		// Step 1: Get the probabilities
		double[] probabilities = this.probabilities.get(NGramDecider.getNGram(ancestors, N_GRAM_LENGTH));
		
		// Step 2: Get the most likely function
		String maxFunction = null;
		double maxProbability = -1.0;
		for(String function : functionChoices) {
			if(maxProbability <= probabilities[this.yFeaturizer.functionIndices.get(function)]) {
				maxFunction = function;
				maxProbability = probabilities[this.yFeaturizer.functionIndices.get(function)];
			}
		}
		
		return maxFunction;
	}
	
	private void build() {
		// Step 1: Build the test set
		List<String> nGrams = new ArrayList<String>();
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(FILENAME));
			
			for(String function0 : this.yFeaturizer.functions) {
				for(String function1 : this.yFeaturizer.functions) {
					// Step 1a: Build the n-gram
					List<String> nGram = new ArrayList<String>();
					nGram.add(function0);
					nGram.add(function1);
					
					// Step 1b: Build the datapoint
					Trio<List<Integer>,List<Integer>,List<Integer>> features = this.xFeaturizer.getFeatures(nGram, this.input, this.output);
					String datapoint = "(" + Utils.toString(features.t0) + ", " + Utils.toString(features.t1) + ", " + Utils.toString(features.t2) + ")";
					
					// Step 1c: Print to test set file
					pw.println(datapoint);
					
					// Step 1d: Add the n-gram to the list
					nGrams.add(Utils.toString(nGram));
				}
			}
			
			pw.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		// Step 2: Get the neural net output
		List<double[]> rawResults = new ArrayList<double[]>();
		try {
			// Step 2a: Execute the Python script
			Process p = Runtime.getRuntime().exec(COMMAND);
			
			// Step 2b: Read the output
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while((line = br.readLine()) != null) {
				if(line.startsWith("RESULT: ")) {
					String[] tokens = line.substring(9, line.length()-1).split(", ");
					double[] rawResult = new double[tokens.length];
					for(int i=0; i<tokens.length; i++) {
						rawResult[i] = Double.parseDouble(tokens[i]);
					}
					if(rawResult.length != this.yFeaturizer.functions.size() + 1) {
						throw new RuntimeException("Invalid number of probabilities!");
					}
					rawResults.add(rawResult);
				}
			}
			br.close();
			
			// Step 2c: Wait for the process to finish
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		// Step 3: Build the mapping
		if(nGrams.size() != rawResults.size()) {
			throw new RuntimeException();
		}
		for(int i=0; i<nGrams.size(); i++) {
			this.probabilities.put(nGrams.get(i), rawResults.get(i));
		}
	}
}
