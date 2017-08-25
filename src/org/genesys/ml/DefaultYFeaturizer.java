package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class DefaultYFeaturizer implements YFeaturizer {
	private final List<String> functions = new ArrayList<String>();
	
	public DefaultYFeaturizer(List<String> functions) {
		this.functions.addAll(functions);
	}
	
	@Override
	public List<Integer> getFeatures(String function) {
		return featurize(function, this.functions);
	}
	
	private static List<Integer> featurize(String curFunction, List<String> functions) {
		List<Integer> features = new ArrayList<Integer>();
		for(String function : functions) {
			features.add(function.equals(curFunction) ? 1 : 0);
		}
		return features;
	}
}
