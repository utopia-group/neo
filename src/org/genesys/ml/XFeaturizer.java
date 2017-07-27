package org.genesys.ml;

import java.util.List;

import org.genesys.models.Pair;

public interface XFeaturizer<T> {
	// (function n-gram, list values)
	public Pair<List<Integer>,List<Integer>> getFeatures(List<String> ancestors, T input, T output);
}
