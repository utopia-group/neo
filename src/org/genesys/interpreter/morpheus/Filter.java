package org.genesys.interpreter.morpheus;

import krangl.ColumnsKt;
import krangl.DataFrame;
import krangl.StringCol;
import org.genesys.interpreter.Binop;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Filter implements Unop {

    private Binop binop;

    private int lhs;

    private int rhs;

    public Filter(Binop bin, int l, int r) {
        binop = bin;
        lhs = l;
        rhs = r;
    }

    public Filter() {

    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        String op = binop.toString();
        DataFrame df = (DataFrame) obj;
        String colName = df.getNames().get(lhs);

        DataFrame res = df.filter((df1, df2) -> {
            if (op.equals("l(a,b).(> a b)")) {
                return ColumnsKt.gt(df.get(colName), rhs);
            } else if (op.equals("l(a,b).(< a b)")) {
                return ColumnsKt.lt(df.get(colName), rhs);
            } else if (op.equals("l(a,b).(== a b)")) {
                return ColumnsKt.eq(df.get(colName), rhs);
            } else {
                throw new UnsupportedOperationException("Unsupported operator:" + op);
            }
        });
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);
        Pair<Boolean, Maybe<Object>> arg2 = args.get(2);
        Pair<Boolean, Maybe<Object>> arg3 = args.get(3);


        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        Binop op = (Binop) arg1.t1.get();
        int lhs = (int) arg2.t1.get();
        int rhs = (Integer) arg3.t1.get();
        if(df.getNcol() <= lhs) return new Pair<>(false, new Maybe<>());
        if(df.getCols().get(lhs) instanceof StringCol) return new Pair<>(false, new Maybe<>());
        String colName = df.getNames().get(lhs);
        String opStr = op.toString();

        DataFrame res = df.filter((df1, df2) -> {
            if (opStr.equals("l(a,b).(> a b)")) {
                return ColumnsKt.gt(df.get(colName), rhs);
            } else if (opStr.equals("l(a,b).(< a b)")) {
                return ColumnsKt.lt(df.get(colName), rhs);
            } else if (opStr.equals("l(a,b).(== a b)")) {
                return ColumnsKt.eq(df.get(colName), rhs);
            } else {
                throw new UnsupportedOperationException("Unsupported OP:" + opStr);
            }
        });
        return new Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(filter " + " x)";
    }
}
