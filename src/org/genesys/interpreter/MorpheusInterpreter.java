package org.genesys.interpreter;

import org.genesys.type.Maybe;

/**
 * interpreter for Morpheus.
 * Created by yufeng on 5/31/17.
 */
public class MorpheusInterpreter implements Interpreter {

    @Override
    public Maybe execute(Object node, Object input) {
        throw new UnsupportedOperationException("Unsupported interpreter: Morpheus.");
    }
}
