package org.genesys.interpreter;

/**
 * Created by yufeng on 5/31/17.
 */
public class PrimitiveBinop implements Binop {
    private final String op;

    public PrimitiveBinop(String op) {
        this.op = op;
    }

    public Object apply(Object first, Object second) {
        if (this.op.equals("+")) {
            return (int) first + (int) second;
        } else if (this.op.equals("*")) {
            return (int) first * (int) second;
        } else if (this.op.equals(">")) {
            return (int) first > (int) second;
        } else if (this.op.equals(">=")) {
            return (int) first >= (int) second;
        } else if (this.op.equals("<")) {
            return (int) first < (int) second;
        } else if (this.op.equals("<=")) {
            return (int) first <= (int) second;
        } else if (this.op.equals("||")) {
            return (boolean) first || (boolean) second;
        } else if (this.op.equals("&&")) {
            return (boolean) first && (boolean) second;
        } else if (this.op.equals("-")) {
            return -(int) first;
        } else if (this.op.equals("~")) {
            return !(boolean) first;
        } else if (this.op.equals("%")) {
            return (int) first % (int) second;
        } else if (this.op.equals("==")) {
            return (int) first == (int) second;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return "l(a,b).(" + this.op + " a b)";
    }
}
