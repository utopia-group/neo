package org.genesys.decide;

import org.genesys.language.Grammar;

/**
 * Created by yufeng on 5/31/17.
 */
public interface ISolver<C, T> {

    T getModel(C core);

}
