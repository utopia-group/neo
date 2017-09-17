package org.genesys.interpreter.morpheus;

import krangl.*;
import org.genesys.interpreter.Binop;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
import org.genesys.utils.MorpheusUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Mutate implements Unop {

    private int lhs;

    private int rhs;

    private Binop binop;

    public Mutate(int l, Binop op, int r) {
        lhs = l;
        binop = op;
        rhs = r;
    }

    public Mutate() {
    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        DataFrame df = (DataFrame) obj;
        String lhsColName = df.getNames().get(lhs);
        String rhsColName = df.getNames().get(rhs);
        String newColName = MorpheusUtil.getInstance().getMorpheusString();
        String opStr = binop.toString();

        DataFrame res = df.mutate(new TableFormula(newColName, (dataFrame, dataFrame2) -> {
            if (opStr.equals("l(a,b).(/ a b)")) {
                return df.get(lhsColName).div(df.get(rhsColName));
            } else {
                throw new UnsupportedOperationException("Unsupported op:" + opStr);

            }
        }));
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
        int lhs = (int) arg1.t1.get();
        Binop op = (Binop) arg2.t1.get();
        int rhs = (int) arg3.t1.get();
        int nCol = df.getNcol();
        if (nCol <= lhs || nCol <= rhs) return new Pair<>(false, new Maybe<>());

        DataCol lhsCol = df.getCols().get(lhs);
        DataCol rhsCol = df.getCols().get(rhs);

        if ((lhsCol instanceof StringCol) || (rhsCol instanceof StringCol)) return new Pair<>(false, new Maybe<>());

        String lhsColName = df.getNames().get(lhs);
        String rhsColName = df.getNames().get(rhs);
        String newColName = MorpheusUtil.getInstance().getMorpheusString();
        String opStr = op.toString();

        DataFrame res = df.mutate(new TableFormula(newColName, (dataFrame, dataFrame2) -> {
            if (opStr.equals("l(a,b).(/ a b)")) {
                return df.get(lhsColName).div(df.get(rhsColName));
            } else {
                throw new UnsupportedOperationException("Unsupported op:" + opStr);

            }
        }));
        return new Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(mutate " + " x)";
    }
}
