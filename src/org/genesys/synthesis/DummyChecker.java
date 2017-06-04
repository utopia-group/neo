package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import org.genesys.models.Example;
import org.genesys.models.Node;

/**
 * Created by yufeng on 6/3/17.
 */
public class DummyChecker<S> implements Checker<Example, BoolExpr> {
    @Override
    public boolean check(Example specification, Node node) {
        return true;
    }

    @Override
    public BoolExpr learnCore() {
        return null;
    }
}
