package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class DefaultYFeaturizer implements YFeaturizer {
	private final List<String> functions = new ArrayList<String>();
	
	public DefaultYFeaturizer(List<String> functions) {
		this.functions.addAll(functions);
	}
	
	@Override
	public double[] getFeatures(String function) {
		return toArray(featurize(function, this.functions));
	}
	
	public static List<Double> featurize(String curFunction, List<String> functions) {
		List<Double> features = new ArrayList<Double>();
		for(String function : functions) {
			features.add(function.equals(curFunction) ? 1.0 : 0.0);
		}
		return features;
	}
	
	public static double[] toArray(List<Double> list) {
		double[] array = new double[list.size()];
		for(int i=0; i<list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
}
