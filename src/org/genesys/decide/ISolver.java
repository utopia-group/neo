package org.genesys.decide;


/**
 * Created by yufeng on 5/31/17.
 */
public interface ISolver<C, T> {

    T getModel(C core);

}
