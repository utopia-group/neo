package org.genesys.ml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genesys.models.Trio;

public class DeepCoderXFeaturizer implements XFeaturizer<Object> {
	private static final Integer NO_VALUE = null;
	public static final String NO_FUNCTION = "";
	public static final int N_GRAM_LENGTH = 2;
	
	private final Map<Integer,Integer> valueLookup = new HashMap<Integer,Integer>();
	private final Map<String,Integer> functionLookup = new HashMap<String,Integer>();
	private final DeepCoderInputSamplerParameters parameters;
	
	public DeepCoderXFeaturizer(DeepCoderInputSamplerParameters parameters, List<String> functions) {
		this.parameters = parameters;
		for(int i=this.parameters.minValue; i<=this.parameters.maxValue; i++) {
			this.valueLookup.put(i, this.valueLookup.size());
		}
		this.valueLookup.put(NO_VALUE, this.valueLookup.size());
		for(String function : functions) {
			this.functionLookup.put(function, this.functionLookup.size());
		}
		this.functionLookup.put(NO_FUNCTION, this.functionLookup.size());
	}
	
	public static List<String> getNGram(List<String> ancestors) {
		List<String> nGram = new ArrayList<String>();
		for(int i=0; i<N_GRAM_LENGTH; i++) {
			int position = ancestors.size()-i-1;
			nGram.add(position >= 0 ? ancestors.get(position) : NO_FUNCTION);
		}
		Collections.reverse(nGram);
		return nGram;
	}

	// (function n-gram, list values)
	@Override
	public Trio<List<Integer>,List<Integer>,List<Integer>> getFeatures(Object input, Object output, List<String> ancestors) {
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
		
		// Step 4: Featurize ancestors
		List<String> nGram = getNGram(ancestors);
		List<Integer> nGramFeatures = new ArrayList<Integer>();
		for(int i=0; i<N_GRAM_LENGTH; i++) {
			nGramFeatures.add(this.functionLookup.get(nGram.get(i)));
		}
		
		return new Trio<List<Integer>,List<Integer>,List<Integer>>(inputValueFeatures, outputValueFeatures, nGramFeatures);
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
