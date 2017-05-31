package org.genesys.decide;

import org.genesys.language.Grammar;

/**
 * Created by yufeng on 5/31/17.
 */
public class BaselineDecider<C, T> implements Decider<C, T> {

    public BaselineDecider() {

    }

    @Override
    public T decide(Grammar g, C core) {
        return null;
    }

    @Override
    public T nextSolution() {
        return null;
    }
}
