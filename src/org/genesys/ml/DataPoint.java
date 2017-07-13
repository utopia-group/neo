package org.genesys.ml;

public class DataPoint {
	public final double[] xFeatures;
	public final double[] yFeatures;
	public DataPoint(double[] xFeatures, double[] yFeatures) {
		this.xFeatures = xFeatures;
		this.yFeatures = yFeatures;
	}
	@Override
	public String toString() {
		return "(" + toString(xFeatures) + ", " + toString(yFeatures) + ")";
	}
	public String toString(double[] ds) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(double d : ds) {
			sb.append(d).append(", ");
		}
		if(ds.length > 0) {
			sb.delete(sb.length()-2, sb.length());
		}
		sb.append("]");
		return sb.toString();
	}
}
