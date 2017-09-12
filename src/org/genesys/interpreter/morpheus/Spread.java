package org.genesys.interpreter.morpheus;

import krangl.DataCol;
import krangl.DataFrame;
import krangl.ReshapeKt;
import krangl.StringCol;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Spread implements Unop {

    private int key;

    private int value;

    public Spread(int k, int v) {
        key = k;
        value = v;
    }

    public Spread() {
    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        DataFrame df = (DataFrame) obj;
        assert df.getNcol() > key;
        assert df.getNcol() > value;
        String keyCol = df.getNames().get(key);
        String valCol = df.getNames().get(value);
        DataFrame res = ReshapeKt.spread(df, keyCol, valCol, null, false);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);
        Pair<Boolean, Maybe<Object>> arg2 = args.get(2);

        if(!arg0.t1.has()) return new Pair<>(true, new Maybe<>());
        DataFrame df = (DataFrame) arg0.t1.get();
        int k = (int) arg1.t1.get();
        int v = (int) arg2.t1.get();
        if ((df.getNcol() <= k) || (df.getNcol() <= v) || (k >= v)) {
            return new Pair<>(false, new Maybe<>());
        } else {
            String keyCol = df.getNames().get(k);
            String valCol = df.getNames().get(v);
            if(!(df.get(keyCol) instanceof StringCol)) return new Pair<>(false, new Maybe<>());
            System.out.println(" \ntable:\n" + df );
            for(DataCol dc : df.getCols()) {
                System.out.println(dc + " " + dc.getClass());
            }
            DataFrame res = ReshapeKt.spread(df, keyCol, valCol, null, false);
            if(res.getNcol() == 0) return new Pair<>(false, new Maybe<>());
            return new Pair<>(true, new Maybe<>(res));
        }
    }

    public String toString() {
        return "l(x).(spread " + " x)";
    }
}
