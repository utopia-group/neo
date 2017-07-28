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
		return "(" + Utils.toString(this.xFunctions) + ", [" + this.xInput + "], [" + this.xOutput + "], [" + this.yFunction + "])";
	}
}
