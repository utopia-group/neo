package org.genesys.ml;

public class L2InputSamplerParameters {
	public final int maxLength;
	public final int maxValue;
	public final int minValue;
	
	public L2InputSamplerParameters(int maxLength, int maxValue, int minValue) {
		this.maxLength = maxLength;
		this.maxValue = maxValue;
		this.minValue = minValue;
	}
}