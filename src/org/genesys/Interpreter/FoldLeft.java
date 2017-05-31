package org.genesys.Interpreter;

import org.genesys.Type.AbstractList;
import org.genesys.Type.Cons;
import org.genesys.Type.EmptyList;

/**
 * Created by yufeng on 5/31/17.
 */
public class FoldLeft implements Unop {
    private final Binop binop;
    private final Object val;

    public FoldLeft(Binop binop, Object val) {
        this.binop = binop;
        this.val = val;
    }

    public Object apply(Object obj) {
        return this.applyRec(obj, this.val);
    }

    private Object applyRec(Object first, Object second) {
        AbstractList list = (AbstractList) first;
        if (list instanceof EmptyList) {
            return second;
        } else {
            Cons cons = (Cons) list;
            return this.applyRec(cons.list, this.binop.apply(cons.obj, second));
        }
    }

    public String toString() {
        return "l(x).(foldLeft " + this.binop.toString() + " x)";
    }
}
