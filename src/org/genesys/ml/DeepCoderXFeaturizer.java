package org.genesys.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genesys.models.Pair;

public class DeepCoderXFeaturizer implements XFeaturizer<Object> {
	private static final Integer NO_VALUE = null;
	
	private final Map<Integer,Integer> valueLookup = new HashMap<Integer,Integer>();
	private final DeepCoderInputSamplerParameters parameters;
	
	public DeepCoderXFeaturizer(DeepCoderInputSamplerParameters parameters) {
		this.parameters = parameters;
		for(int i=this.parameters.minValue; i<=this.parameters.maxValue; i++) {
			this.valueLookup.put(i, this.valueLookup.size());
		}
		this.valueLookup.put(NO_VALUE, this.valueLookup.size());
	}

	// (function n-gram, list values)
	@Override
	public Pair<List<Integer>,List<Integer>> getFeatures(Object input, Object output) {
		// Step 1: Flatten input and output
		List<Integer> flatInput = new ArrayList<Integer>();
		List<Integer> flatOutput = new ArrayList<Integer>();
		this.flatten(((List<Integer>)input).get(0), flatInput);
		this.flatten(output, flatOutput);
		
		// Step 2: Featurize input example
		List<Integer> inputValueFeatures = new ArrayList<Integer>();
		for(int i=0; i<this.parameters.maxLength; i++) {
			Integer curValue = flatInput.size() > i ? flatInput.get(i) : NO_VALUE;
			inputValueFeatures.add(this.valueLookup.get(curValue));
		}
		
		// Step 3: Featurize output example
		List<Integer> outputValueFeatures = new ArrayList<Integer>();
		for(int i=0; i<this.parameters.maxLength; i++) {
			Integer curValue = flatOutput.size() > i ? flatOutput.get(i) : NO_VALUE;
			outputValueFeatures.add(this.valueLookup.getOrDefault(curValue, this.valueLookup.get(NO_VALUE)));
		}
		
		return new Pair<List<Integer>,List<Integer>>(inputValueFeatures, outputValueFeatures);
	}
	
	private void flatten(Object t, List<Integer> result) {
		if(t instanceof List) {
			result.addAll((List<Integer>)t);
		} else if(t instanceof Integer) {
			result.add((Integer)t);
		} else {
			throw new RuntimeException("Type not handled: " + t.getClass());
		}
	}
}
