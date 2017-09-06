package org.genesys.interpreter.morpheus;

import kotlin.jvm.functions.Function2;
import krangl.DataFrame;
import krangl.TableFormula;
import org.genesys.interpreter.Unop;
import org.genesys.utils.MorpheusUtil;

import java.util.ArrayList;
import java.util.List;

import static krangl.ColumnsKt.asDoubles;
import static krangl.ColumnsKt.min;
import static krangl.MathHelpersKt.cumSum;
import static krangl.MathHelpersKt.mean;


/**
 * Created by yufeng on 9/3/17.
 */
public class Summarise implements Unop {

    private String aggr;

    private int colVal;

    public Summarise(String a, int c) {
        aggr = a;
        colVal = c;
    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        DataFrame df = (DataFrame) obj;
        String colName = df.getNames().get(colVal);
        String newColName = MorpheusUtil.getInstance().getMorpheusString();

        DataFrame res = df.summarize(new TableFormula(newColName, (dataFrame, dataFrame2) -> {
            if (aggr.equals("mean")) {
                return mean(asDoubles(df.get(colName)));
            } else if (aggr.equals("sum")) {
                List list = new ArrayList();
                list.add(df.get(colName));
                //FIXME: cumSum != sum.
                return cumSum(list, false);
            } else if (aggr.equals("min")) {
                return min(df.get(colName), false);
            } else {
                throw new UnsupportedOperationException("Unsupported aggregator:" + aggr);
            }
        }));
        return res;
    }

    public String toString() {
        return "l(x).(select " + " x)";
    }
}
