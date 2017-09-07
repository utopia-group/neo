package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;

import java.util.List;

/**
 * Created by yufeng on 9/7/17.
 */
public class AccessUnop implements Unop {

    public Object apply(Object obj) {
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof List;
        assert pair.get(1) instanceof Integer;
        List xs = (List) pair.get(0);
        int n = (Integer) pair.get(1);
        Object res = null;
        if (xs.size() > n) {
            res = xs.get(n);
        }
        return res;
    }

    public String toString() {
        return "ACCESS";
    }
}
