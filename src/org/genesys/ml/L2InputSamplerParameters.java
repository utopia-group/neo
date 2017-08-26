package org.genesys.ml;

public class L2InputSamplerParameters {
	public final int minLength;
	public final int maxLength;
	public final int maxValue;
	public final int minValue;
	
	public L2InputSamplerParameters(int minLength, int maxLength, int maxValue, int minValue) {
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.maxValue = maxValue;
		this.minValue = minValue;
	}
}