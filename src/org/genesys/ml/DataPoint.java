package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class DataPoint {
	public final List<Integer> xFunctionFeatures = new ArrayList<Integer>();
	public final List<Integer> xValueFeatures = new ArrayList<Integer>();
	public final List<Double> yFeatures = new ArrayList<Double>();
	public DataPoint(List<Integer> xFunctionFeatures, List<Integer> xValueFeatures, List<Double> yFeatures) {
		this.xFunctionFeatures.addAll(xFunctionFeatures);
		this.xValueFeatures.addAll(xValueFeatures);
		this.yFeatures.addAll(yFeatures);
	}
	@Override
	public String toString() {
		return "(" + toString(this.xFunctionFeatures) + ", " + toString(this.xValueFeatures) + ", " + toString(this.yFeatures) + ")";
	}
	public <T> String toString(List<T> ts) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(T t : ts) {
			sb.append(t).append(", ");
		}
		if(ts.size() > 0) {
			sb.delete(sb.length()-2, sb.length());
		}
		sb.append("]");
		return sb.toString();
	}
}
