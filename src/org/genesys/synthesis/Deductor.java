package org.genesys.synthesis;

import org.genesys.utils.Node;

/**
 * Created by yufeng on 5/28/17.
 */
public class Deductor<S> implements Checker<S> {
    @Override
    public boolean check(S specification, Node node) {
        return true;
    }
}
