package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

public class L2XFeaturizer implements XFeaturizer<Object> {
	private static final String NO_FUNCTION = "";
	
	private final List<String> functions = new ArrayList<String>();
	private final int nGramLength;
	
	public L2XFeaturizer(List<String> functions, int nGramLength) {
		for(String function : functions) {
			if(function.equals(NO_FUNCTION)) {
				throw new RuntimeException("Error: Empty function name!");
			}
		}
		this.functions.addAll(functions);
		this.nGramLength = nGramLength;
	}
	
	@Override
	public double[] getFeatures(List<String> ancestors, Object input, Object output) {
		// Step 1: Flatten input and output
		List<Integer> flatInput = new ArrayList<Integer>();
		List<Integer> flatOutput = new ArrayList<Integer>();
		this.flatten(input, flatInput);
		this.flatten(output, flatOutput);
		
		// Step 2: Featurize
		List<Double> features = new ArrayList<Double>();
		
		// Step 2a: Featurize ancestors
		for(int i=0; i<this.nGramLength; i++) {
			String curFunction = ancestors.size() > i ? ancestors.get(ancestors.size() - i - 1) : NO_FUNCTION;
			features.addAll(DefaultYFeaturizer.featurize(curFunction, this.functions));
		}
		
		// Step 2b: Featurize input-output example
		features.add((double)max(flatInput, 0)/max(flatOutput, 0));
		features.add((double)min(flatInput, 0)/min(flatOutput, 0));
		
		// Step 3: Convert to array
		return DefaultYFeaturizer.toArray(features);
	}
	
	private static int max(List<Integer> ints, int defaultInt) {
		Integer max = null;
		for(int i : ints) {
			if(max == null || i > max) {
				max = i;
			}
		}
		return max == null ? defaultInt : max;
	}
	
	private static int min(List<Integer> ints, int defaultInt) {
		Integer min = null;
		for(int i : ints) {
			if(min == null || i < min) {
				min = i;
			}
		}
		return min == null ? defaultInt : min;
	}
	
	private void flatten(Object t, List<Integer> result) {
		if(t instanceof AbstractList) {
			while(!(t instanceof EmptyList)) {
				flatten(((Cons)t).obj, result);
				t = ((Cons)t).list;
			}
		} else if(t instanceof Integer) {
			result.add((Integer)t);
		} else {
			throw new RuntimeException("Type not handled: " + t.getClass());
		}
	}
}
