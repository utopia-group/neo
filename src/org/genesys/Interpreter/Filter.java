package org.genesys.Interpreter;

import org.genesys.Type.AbstractList;
import org.genesys.Type.Cons;
import org.genesys.Type.EmptyList;

/**
 * Created by yufeng on 5/31/17.
 */
public class Filter implements Unop {
    private final Unop unop;

    public Filter(Unop unop) {
        this.unop = unop;
    }

    public Object apply(Object obj) {
        AbstractList list = (AbstractList) obj;
        if (list instanceof EmptyList) {
            return list;
        } else {
            Cons cons = (Cons) list;
            if ((boolean) this.unop.apply(cons.obj)) {
                return new Cons(cons.obj, (AbstractList) this.apply(cons.list));
            } else {
                return this.apply(cons.list);
            }
        }
    }

    public String toString() {
        return "l(x).(filter " + this.unop.toString() + " x)";
    }
}
