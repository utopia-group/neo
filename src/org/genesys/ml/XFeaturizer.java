package org.genesys.ml;

import java.util.List;

public interface XFeaturizer<T> {
	public double[] getFeatures(List<String> ancestors, T input, T output);
}
