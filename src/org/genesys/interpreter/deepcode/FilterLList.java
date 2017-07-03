package org.genesys.interpreter.deepcode;

import org.genesys.interpreter.Unop;
import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

import java.util.LinkedList;

/**
 * Created by yufeng on 5/31/17.
 */
public class FilterLList implements Unop {
    private final Unop unop;

    public FilterLList(Unop unop) {
        this.unop = unop;
    }

    public Object apply(Object obj) {
        LinkedList list = (LinkedList)  obj;
        if (list.isEmpty()) {
            return list;
        } else {
            LinkedList targetList = new LinkedList();
            for (Object elem : list) {
                if ((boolean) this.unop.apply(elem)) {
                    targetList.add(elem);
                }
            }
            return targetList;
        }
    }

    public String toString() {
        return "l(x).(filter " + this.unop.toString() + " x)";
    }
}
