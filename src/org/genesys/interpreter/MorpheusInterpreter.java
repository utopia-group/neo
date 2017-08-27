package org.genesys.interpreter;

import org.genesys.type.Maybe;

import java.util.Set;

/**
 * interpreter for Morpheus.
 * Created by yufeng on 5/31/17.
 */
public class MorpheusInterpreter implements Interpreter {

    @Override
    public Maybe execute(Object node, Object input) {
        throw new UnsupportedOperationException("Unsupported interpreter: Morpheus.");
    }

    @Override
    public Set<String> getExeKeys() {
        throw new UnsupportedOperationException("Unsupported interpreter: Default.");
    }
}
