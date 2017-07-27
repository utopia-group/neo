package org.genesys.ml;

public class L2XFeaturizerParameters {
	public final L2InputSamplerParameters sampler;
	public final int nGramLength;
	public L2XFeaturizerParameters(L2InputSamplerParameters sampler, int nGramLength) {
		this.sampler = sampler;
		this.nGramLength = nGramLength;
	}
}
