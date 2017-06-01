package org.genesys.decide;

import org.genesys.language.Grammar;

/**
 * Created by yufeng on 5/31/17.
 */
public class BaselineSolver<C, T> implements ISolver<C, T> {

    public BaselineSolver(Grammar g) {

    }

    @Override
    public T getModel(C core) {
        return null;
    }

}
