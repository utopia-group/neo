package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class Datapoint {
	public final List<Integer> xFunctionFeatures = new ArrayList<Integer>();
	public final List<Integer> xValueFeatures = new ArrayList<Integer>();
	public final List<Double> yFeatures = new ArrayList<Double>();
	public Datapoint(List<Integer> xFunctionFeatures, List<Integer> xValueFeatures, List<Double> yFeatures) {
		this.xFunctionFeatures.addAll(xFunctionFeatures);
		this.xValueFeatures.addAll(xValueFeatures);
		this.yFeatures.addAll(yFeatures);
	}
	@Override
	public String toString() {
		return "(" + Utils.toString(this.xFunctionFeatures) + ", " + Utils.toString(this.xValueFeatures) + ", " + Utils.toString(this.yFeatures) + ")";
	}
}
