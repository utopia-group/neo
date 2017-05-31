package org.genesys.synthesis;

import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.models.Node;

/**
 * Created by yufeng on 5/28/17.
 */
public interface Synthesizer {
    <T, S> Node synthesize(Grammar<T> grammar, S problem, Checker<S> checker, Interpreter... interpreter);

    Node nextSolution();
}