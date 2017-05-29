package org.genesys.synthesis;

import org.genesys.language.Grammar;
import org.genesys.utils.Node;

/**
 * Created by yufeng on 5/28/17.
 */
public interface Synthesizer {
    public <T, S> Node synthesize(Grammar<T> grammar, S problem, Checker<S> checker);

    public Node nextSolution();
}