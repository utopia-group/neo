package org.genesys.ml;

public class L2InputSamplerParameters {
	public final int maxLength;
	public final int maxInt;
	public final int minInt;
	
	public L2InputSamplerParameters(int maxLength, int maxInt, int minInt) {
		this.maxLength = maxLength;
		this.maxInt = maxInt;
		this.minInt = minInt;
	}
}