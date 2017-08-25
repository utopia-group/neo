package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class Datapoint {
	public final List<Integer> xFunctionFeatures = new ArrayList<Integer>();
	public final List<Integer> xInputValueFeatures = new ArrayList<Integer>();
	public final List<Integer> xOutputValueFeatures = new ArrayList<Integer>();
	public final List<Integer> yFeatures = new ArrayList<Integer>();
	public Datapoint(List<Integer> xFunctionFeatures, List<Integer> xInputValueFeatures, List<Integer> xOutputValueFeatures, List<Integer> yFeatures) {
		this.xFunctionFeatures.addAll(xFunctionFeatures);
		this.xInputValueFeatures.addAll(xInputValueFeatures);
		this.xOutputValueFeatures.addAll(xOutputValueFeatures);
		this.yFeatures.addAll(yFeatures);
	}
	@Override
	public String toString() {
		return "(" + Utils.toString(this.xFunctionFeatures) + ", " + Utils.toString(this.xInputValueFeatures) + ", " + Utils.toString(this.xOutputValueFeatures) + ", " + Utils.toString(this.yFeatures) + ")";
	}
}
