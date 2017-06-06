package org.genesys.interpreter;

import org.genesys.type.Maybe;

/**
 * Can be used in BigLambda, Morpheus, and SQL.
 * Created by yufeng on 5/31/17.
 */
public class SparkInterpreter implements Interpreter {

    @Override
    public Maybe execute(Object node, Object input) {
        throw new UnsupportedOperationException("Unsupported interpreter: Spark.");
    }
}
