package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
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
        DataFrame df = (DataFrame) obj;
        if ((df.getNcol() <= key) || (df.getNcol() <= value) || (key == value)) {
            return new Pair<>(false, new Maybe<>());
        } else {
            String keyCol = df.getNames().get(key);
            String valCol = df.getNames().get(value);
            DataFrame res = ReshapeKt.spread(df, keyCol, valCol, null, false);
            return new Pair<>(true, new Maybe<>(res));
        }
    }

    public String toString() {
        return "l(x).(spread " + " x)";
    }
}
