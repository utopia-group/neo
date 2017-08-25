package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;
import org.genesys.utils.LibUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by yufeng on 6/4/17.
 */
public class SortUnop implements Unop {

    public Object apply(Object obj) {
        assert obj instanceof List : obj;
        List<Integer> list = LibUtils.cast(obj);
        Collections.sort(list);
        return list;
    }

    public String toString() {
        return "sort ( " + ")";
    }
}