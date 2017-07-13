package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;
import org.genesys.utils.LibUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by yufeng on 6/4/17.
 */
public class SumUnop implements Unop {

    public Object apply(Object obj) {
        assert obj instanceof List : obj;
        List<Integer> list = LibUtils.cast(obj);
        Optional<Integer> sum = list.stream().reduce(Integer::sum);
        return sum.get();
    }

    public String toString() {
        return "sum ( " + ")";
    }
}
