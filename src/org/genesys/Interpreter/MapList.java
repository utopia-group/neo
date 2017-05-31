package org.genesys.Interpreter;

import org.genesys.Type.AbstractList;
import org.genesys.Type.Cons;
import org.genesys.Type.EmptyList;

/**
 * Created by yufeng on 5/31/17.
 */
public class MapList implements Unop {
    private final Unop unop;

    public MapList(Unop unop) {
        this.unop = unop;
    }

    public Object apply(Object obj) {
        AbstractList list = (AbstractList) obj;
        if (list instanceof EmptyList) {
            return list;
        } else {
            Cons cons = (Cons) list;
            return new Cons(this.unop.apply(cons.obj), (AbstractList) this.apply(cons.list));
        }
    }

    public String toString() {
        return "l(x).(map " + this.unop.toString() + " x)";
    }
}
