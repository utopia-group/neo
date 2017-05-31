package org.genesys.decide;

import org.genesys.language.Grammar;

/**
 * Created by yufeng on 5/31/17.
 */
public interface Decider<C, T> {

    T decide(Grammar g, C core);

    T nextSolution();
}
