package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;
import org.genesys.utils.LibUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by yufeng on 6/4/17.
 */
public class LastUnop implements Unop {

    public Object apply(Object obj) {
        assert obj instanceof List : obj;
        List<Integer> list = LibUtils.cast(obj);
        int len = list.size();
        return (len == 0) ? null : list.get(len - 1);
    }

    public String toString() {
        return "LAST";
    }
}
