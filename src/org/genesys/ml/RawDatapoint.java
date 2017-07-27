package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

public class RawDatapoint<T> {
	public final List<String> xFunctions = new ArrayList<String>();
	public final T xInput;
	public final T xOutput;
	public final String yFunction;
	public RawDatapoint(List<String> xFunctions, T xInput, T xOutput, String yFunction) {
		this.xFunctions.addAll(xFunctions);
		this.xInput = xInput;
		this.xOutput = xOutput;
		this.yFunction = yFunction;
	}
	@Override
	public String toString() {
		return "(" + toString(this.xFunctions) + ", [" + this.xInput + "], [" + this.xOutput + "], [" + this.yFunction + "])";
	}
	public <U> String toString(List<U> ts) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(U t : ts) {
			sb.append(t).append(", ");
		}
		if(ts.size() > 0) {
			sb.delete(sb.length()-2, sb.length());
		}
		sb.append("]");
		return sb.toString();
	}
}
