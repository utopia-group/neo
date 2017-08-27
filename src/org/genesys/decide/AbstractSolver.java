package org.genesys.decide;

import java.util.List;
import org.genesys.models.Pair;

/**
 * Created by yufeng on 5/31/17.
 */
public interface AbstractSolver<C, T> {

    T getModel(C core);

    T getCoreModel(List<Pair<Integer, List<String>>> core);

    boolean isPartial();

}
