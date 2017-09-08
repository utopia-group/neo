package org.genesys.ml;

public class RawDatapoint<T> {
	public final T xInput;
	public final T xOutput;
	public final String yFunction;
	public RawDatapoint(T xInput, T xOutput, String yFunction) {
		this.xInput = xInput;
		this.xOutput = xOutput;
		this.yFunction = yFunction;
	}
	@Override
	public String toString() {
		return "([" + this.xInput + "], [" + this.xOutput + "], [" + this.yFunction + "])";
	}
}
