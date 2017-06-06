package org.genesys.interpreter;

import org.genesys.models.Node;
import org.genesys.type.Maybe;

/**
 * Created by yufeng on 5/30/17.
 */
public class DefaultInterpreter implements Interpreter {

    /**
     * Define your own interpreter here.
     */
    public DefaultInterpreter() {

    }

    @Override
    public Maybe execute(Object node, Object input) {
        throw new UnsupportedOperationException("Unsupported interpreter: Default.");
    }
}
