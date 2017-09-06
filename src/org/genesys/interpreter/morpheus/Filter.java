package org.genesys.interpreter.morpheus;

import krangl.ColumnsKt;
import krangl.DataFrame;
import org.genesys.interpreter.Binop;
import org.genesys.interpreter.Unop;

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

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        String op = binop.toString();
        DataFrame df = (DataFrame) obj;
        String colName = df.getNames().get(lhs);

        DataFrame res = df.filter((df1, df2) -> {
            if (op.equals("l(a,b).(> a b)")) {
                return ColumnsKt.gt(df.get(colName), rhs);
            } else {
                throw new UnsupportedOperationException("Unsupported operator:" + op);
            }
        });
        return res;
    }

    public String toString() {
        return "l(x).(filter " + " x)";
    }
}
