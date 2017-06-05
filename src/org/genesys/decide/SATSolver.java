package org.genesys.decide;

import org.genesys.language.Grammar;

/**
 * Created by yufeng on 5/31/17.
 */
public class SATSolver<C, T> implements AbstractSolver<C, T> {

    private Decider optimizer;

    public SATSolver(Grammar g) {
        optimizer = new MLDecider();
    }

    @Override
    public T getModel(C core) {
        return null;
    }
}
