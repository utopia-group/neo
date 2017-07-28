package org.genesys.ml;

import java.util.List;

public interface Decider<T> {
	public String decide(List<String> ancestors, T input, T output, List<String> functionChoices);
}
