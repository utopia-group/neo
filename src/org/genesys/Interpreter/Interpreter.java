package org.genesys.Interpreter;

import synth.utils.Utils;

import java.util.List;

/**
 * Created by yufeng on 5/30/17.
 */
public interface Interpreter {
    <T> Object execute(T node);
}
