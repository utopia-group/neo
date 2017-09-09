package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;
import org.genesys.utils.LibUtils;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by yufeng on 6/4/17.
 */
public class ReverseUnop implements Unop {

    public Object apply(Object obj) {
        assert obj instanceof List : obj;
        List<Integer> list = LibUtils.cast(obj);
        // Make a deep copy
        List<Integer> reverse = new ArrayList<>();
        for (Integer i : list)
            reverse.add(i);
        Collections.reverse(reverse);
        return reverse;
    }

    public String toString() {
        return "REVERSE";
    }
}
