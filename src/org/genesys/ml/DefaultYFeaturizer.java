package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class DefaultYFeaturizer implements YFeaturizer {
	private final List<String> functions = new ArrayList<String>();
	
	public DefaultYFeaturizer(List<String> functions) {
		this.functions.addAll(functions);
	}
	
	@Override
	public List<Double> getFeatures(String function) {
		return featurize(function, this.functions);
	}
	
	private static List<Double> featurize(String curFunction, List<String> functions) {
		List<Double> features = new ArrayList<Double>();
		for(String function : functions) {
			features.add(function.equals(curFunction) ? 1.0 : 0.0);
		}
		return features;
	}
}
