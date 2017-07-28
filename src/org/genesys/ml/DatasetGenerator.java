package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.genesys.interpreter.Interpreter;
import org.genesys.models.Node;
import org.genesys.models.Pair;

public class DatasetGenerator {
	public static <T> List<RawDatapoint<T>> generateDataset(
			Interpreter<Node,T> interpreter,
			Sampler<Node> programSampler,
			Sampler<T> inputSampler) {
		
		List<RawDatapoint<T>> dataset = new ArrayList<RawDatapoint<T>>();
		
		// Step 1: Sample program and input, and obtain output
		Node program = programSampler.sample();
		System.out.println("SAMPLED PROGRAM: " + program);
		T input = inputSampler.sample();
		System.out.println("SAMPLED INPUT: " + input);
		T output = interpreter.execute(program, input).get();
		System.out.println("COMPUTED OUTPUT: " + output);
		
		// Step 2: Construct n-grams
		List<Pair<String,List<String>>> ancestors = getFunctionData(program);
		
		// Step 3: Construct data points
		for(Pair<String,List<String>> ancestor : ancestors) {
			RawDatapoint<T> datapoint = new RawDatapoint<T>(ancestor.t1, input, output, ancestor.t0);
			dataset.add(datapoint);
		}
		
		return dataset;
	}
	
	public static <T> List<Datapoint> translateDataset(
			List<RawDatapoint<T>> rawDataset,
			XFeaturizer<T> xFeaturizer,
			YFeaturizer yFeaturizer) {
		List<Datapoint> dataset = new ArrayList<Datapoint>();
		for(RawDatapoint<T> rawDataPoint : rawDataset) {
			Pair<List<Integer>,List<Integer>> xFeatures = xFeaturizer.getFeatures(rawDataPoint.xFunctions, rawDataPoint.xInput, rawDataPoint.xOutput);
			List<Double> yFeatures = yFeaturizer.getFeatures(rawDataPoint.yFunction);
			Datapoint datapoint = new Datapoint(xFeatures.t0, xFeatures.t1, yFeatures);
			dataset.add(datapoint);
		}
		return dataset;
	}
	
	public static List<Pair<String,List<String>>> getFunctionData(Node program) {
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
