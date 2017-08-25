package org.genesys.ml;

import java.util.List;

public interface YFeaturizer {
	public List<Integer> getFeatures(String function);
}
