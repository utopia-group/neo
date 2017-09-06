package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
import org.genesys.interpreter.Unop;

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

    public String toString() {
        return "l(x).(spread " + " x)";
    }
}
