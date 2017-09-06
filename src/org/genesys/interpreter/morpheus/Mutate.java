package org.genesys.interpreter.morpheus;

import krangl.ColumnsKt;
import krangl.DataFrame;
import krangl.ReshapeKt;
import krangl.TableFormula;
import org.genesys.interpreter.Binop;
import org.genesys.interpreter.Unop;
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

    public String toString() {
        return "l(x).(mutate " + " x)";
    }
}
