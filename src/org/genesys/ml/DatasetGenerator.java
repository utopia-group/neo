package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.genesys.interpreter.Interpreter;
import org.genesys.models.Node;
import org.genesys.models.Pair;

public class DatasetGenerator {
	public static <T> List<DataPoint> generateDataset(
			Interpreter<Node,T> interpreter,
			XFeaturizer<T> xFeaturizer,
			YFeaturizer yFeaturizer,
			Sampler<Node> programSampler,
			Sampler<T> inputSampler) {
		
		List<DataPoint> dataset = new ArrayList<DataPoint>();
		
		// Step 1: Sample program and input, and obtain output
		Node program = programSampler.sample();
		T input = inputSampler.sample();
		T output = interpreter.execute(program, input).get();
		
		// Step 2: Construct n-grams
		List<Pair<String,List<String>>> ancestors = getFunctionData(program);
		
		// Step 3: Construct data points
		for(Pair<String,List<String>> ancestor : ancestors) {
			double[] xFeatures = xFeaturizer.getFeatures(ancestor.t1, input, output);
			double[] yFeatures = yFeaturizer.getFeatures(ancestor.t0);
			DataPoint dataPoint = new DataPoint(xFeatures, yFeatures);
			dataset.add(dataPoint);
		}
		
		return dataset;
	}
	
	private static List<Pair<String,List<String>>> getFunctionData(Node program) {
		List<Pair<String,List<String>>> data = new ArrayList<Pair<String,List<String>>>();
		Stack<String> stack = new Stack<String>();
		getFunctionDataHelper(program, stack, data);
		return data;
	}
	
	private static void getFunctionDataHelper(Node program, Stack<String> stack, List<Pair<String,List<String>>> data) {
		data.add(new Pair<String,List<String>>(program.function, new ArrayList<String>(stack)));
		stack.push(program.function);
		for(Node child : program.children) {
			getFunctionDataHelper(child, stack, data);
		}
		stack.pop();
	}
}
