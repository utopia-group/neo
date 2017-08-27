package org.genesys.ml;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import org.genesys.decide.Decider;
import org.genesys.models.Trio;

public class DeepCoderPythonDecider implements Decider {
	private static final String FILENAME = "./model/tmp/deep_coder.txt";
	private static final String COMMAND = "/usr/local/bin/python -m model.genesys.test";
	
	private final XFeaturizer<Object> xFeaturizer;
	private final YFeaturizer yFeaturizer;
	private final Object input;
	private final Object output;
	
	public DeepCoderPythonDecider(XFeaturizer<Object> xFeaturizer, YFeaturizer yFeaturizer, Object input, Object output) {
		this.xFeaturizer = xFeaturizer;
		this.yFeaturizer = yFeaturizer;
		this.input = input;
		this.output = output;
	}
	
	@Override
	public String decide(List<String> ancestors, List<String> functionChoices) {
		// Step 1: Get the probabilities
		double[] probabilities = this.runPythonScript(ancestors);
		if(probabilities.length != this.yFeaturizer.functions.size()+1) { // +1 is for NO_FUNCTION option
			throw new RuntimeException("Invalid result length!");
		}
		
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
	
	private double[] runPythonScript(List<String> ancestors) {
		// Step 1: Build the datapoint
		Trio<List<Integer>,List<Integer>,List<Integer>> features = this.xFeaturizer.getFeatures(ancestors, this.input, this.output);
		String datapoint = "(" + Utils.toString(features.t0) + ", " + Utils.toString(features.t1) + ", " + Utils.toString(features.t2) + ")";
		
		// Step 2: Print to file
		try {
			PrintWriter pw = new PrintWriter(new FileWriter(FILENAME));
			pw.print(datapoint);
			pw.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		
		// Step 3: Execute the python script
		String[] tokens = null;
		try {
			Process p = Runtime.getRuntime().exec(COMMAND);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while((line = br.readLine()) != null) {
				if(line.startsWith("RESULT: ")) {
					tokens = line.substring(9, line.length()-1).split(", ");
				}
			}
			br.close();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		if(tokens == null) {
			throw new RuntimeException();
		}
		
		// Step 4: Convert to float array
		double[] result = new double[tokens.length];
		for(int i=0; i<tokens.length; i++) {
			result[i] = Double.parseDouble(tokens[i]);
		}
		
		return result;
	}
}
