package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.TableFormula;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
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

    public Summarise() {
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
                //FIXME: cumSum != sum. I will fix it
                return mean(asDoubles(df.get(colName)));
//                return cumSum(list, false);
            } else if (aggr.equals("min")) {
                return min(df.get(colName), false);
            } else {
                throw new UnsupportedOperationException("Unsupported aggregator:" + aggr);
            }
        }));
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);
        Pair<Boolean, Maybe<Object>> arg2 = args.get(2);

        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        String aggr = (String) arg1.t1.get();
        int colIdx = (int) arg2.t1.get();
        if (df.getNcol() <= colIdx) return new Pair<>(false, new Maybe<>());

        String colName = df.getNames().get(colIdx);
        String newColName = MorpheusUtil.getInstance().getMorpheusString();

        DataFrame res = df.summarize(new TableFormula(newColName, (dataFrame, dataFrame2) -> {
            if (aggr.equals("mean")) {
                return mean(asDoubles(df.get(colName)));
            } else if (aggr.equals("sum")) {
                List list = new ArrayList();
                list.add(df.get(colName));
                //FIXME: cumSum != sum. fix this later
//                return cumSum(list, false);
                return mean(asDoubles(df.get(colName)));

            } else if (aggr.equals("min")) {
                return min(df.get(colName), false);
            } else {
                throw new UnsupportedOperationException("Unsupported aggr:" + aggr);
            }
        }));

        return new Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(summarise " + " x)";
    }
}
