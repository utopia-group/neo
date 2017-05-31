package org.genesys.synthesis;

import org.genesys.models.Node;

/**
 * Created by yufeng on 5/28/17.
 */
public interface Checker<S> {
    public boolean check(S specification, Node node);
}