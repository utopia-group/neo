package org.genesys.decide;

/**
 * Created by yufeng on 5/31/17.
 */
public class SATDecider implements Decider {

    private Optimizer optimizer;

    public SATDecider() {
        optimizer = new MLOptimizer();
    }
}
