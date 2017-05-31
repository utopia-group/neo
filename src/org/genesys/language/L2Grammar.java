package org.genesys.language;

import java.util.List;

/**
 * Created by yufeng on 5/31/17.
 */
public class L2Grammar<T> implements Grammar<T> {

    @Override
    public T start() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<Production<T>> getProductions() {
        return null;
    }

    @Override
    public List<Production> productionsFor(Object symbol) {
        return null;
    }
}
