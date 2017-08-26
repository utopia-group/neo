package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class YFeaturizer {
	public final List<String> functions = new ArrayList<String>();
	
	public YFeaturizer(List<String> functions) {
		this.functions.addAll(functions);
	}
	
	public List<Integer> getFeatures(String function) {
		return featurize(function, this.functions);
	}
	
	private static List<Integer> featurize(String curFunction, List<String> functions) {
		List<Integer> features = new ArrayList<Integer>();
		for(String function : functions) {
			features.add(function.equals(curFunction) ? 1 : 0);
		}
		// handle the "no function" case
		features.add(0);
		return features;
	}
}
