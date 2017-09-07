package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 5/31/17.
 */
public class CountList implements Unop {
    private final Unop unop;

    public CountList(Unop unop) {
        this.unop = unop;
    }

    public Object apply(Object obj) {
        List list = (List) obj;
        int cnt = 0;
        if (list.isEmpty()) {
            return cnt;
        } else {
            for (Object elem : list) {
                if ((boolean) this.unop.apply(elem)) {
                    cnt++;
                }
            }
            return cnt;
        }
    }

    public String toString() {
        return "COUNT";
    }
}
