package org.genesys.interpreter;

/**
 * Created by yufeng on 5/31/17.
 */
public class PrimitiveUnop implements Unop {
    private final PrimitiveBinop op;
    private final Object val;
    public PrimitiveUnop(String op, Object val) {
        this.op = new PrimitiveBinop(op);
        this.val = val;
    }
    public Object apply(Object obj) {
        return this.op.apply(obj, this.val);
    }
    @Override
    public String toString() {
        if(op.equals("-") || op.equals("~")) {
            return "l(a).(" + this.op + " a)";
        } else {
            return "l(a).(" + this.op + " (" + this.val.toString() + ") b)";
        }
    }
}
