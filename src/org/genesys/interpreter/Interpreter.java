package org.genesys.interpreter;

/**
 * Created by yufeng on 5/30/17.
 */
public interface Interpreter {
    <T> Object execute(T node);
}
