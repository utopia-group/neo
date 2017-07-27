package org.genesys.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genesys.models.Pair;
import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

public class L2XFeaturizer implements XFeaturizer<Object> {
	private static final String NO_FUNCTION = "";
	private static final Integer NO_VALUE = null;
	
	private final Map<String,Integer> functionLookup = new HashMap<String,Integer>();
	private final Map<Integer,Integer> valueLookup = new HashMap<Integer,Integer>();
	private final L2XFeaturizerParameters parameters;
	
	public L2XFeaturizer(List<String> functions, L2XFeaturizerParameters parameters) {
		this.parameters = parameters;
		for(String function : functions) {
			if(function.equals(NO_FUNCTION)) {
				throw new RuntimeException("Error: Empty function name!");
			}
		}
		for(String function : functions) {
			this.functionLookup.put(function, this.functionLookup.size());
		}
		this.functionLookup.put(NO_FUNCTION, this.functionLookup.size());
		for(int i=this.parameters.sampler.minValue; i<=this.parameters.sampler.maxValue; i++) {
			this.valueLookup.put(i, this.valueLookup.size());
		}
		this.valueLookup.put(NO_VALUE, this.valueLookup.size());
	}

	// (function n-gram, list values)
	@Override
	public Pair<List<Integer>,List<Integer>> getFeatures(List<String> ancestors, Object input, Object output) {
		// Step 1: Featurize ancestors
		List<Integer> functionFeatures = new ArrayList<Integer>();
		for(int i=0; i<this.parameters.nGramLength; i++) {
			String curFunction = ancestors.size() > i ? ancestors.get(ancestors.size() - i - 1) : NO_FUNCTION;
			functionFeatures.add(this.functionLookup.get(curFunction));
		}
		
		// Step 2: Flatten input and output
		List<Integer> flatInput = new ArrayList<Integer>();
		List<Integer> flatOutput = new ArrayList<Integer>();
		this.flatten(input, flatInput);
		this.flatten(output, flatOutput);
		
		// Step 3: Featurize input-output example
		List<Integer> valueFeatures = new ArrayList<Integer>();
		for(int i=0; i<this.parameters.sampler.maxLength; i++) {
			Integer curValue = flatInput.size() > i ? flatInput.get(i) : NO_VALUE;
			valueFeatures.add(this.valueLookup.get(curValue));
		}
		for(int i=0; i<this.parameters.sampler.maxLength; i++) {
			Integer curValue = flatOutput.size() > i ? flatOutput.get(i) : NO_VALUE;
			valueFeatures.add(this.valueLookup.get(curValue));
		}
		
		return new Pair<List<Integer>,List<Integer>>(functionFeatures, valueFeatures);
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
