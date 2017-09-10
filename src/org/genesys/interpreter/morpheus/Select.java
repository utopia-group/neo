package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
import org.genesys.interpreter.Binop;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Select implements Unop {

    public Object apply(Object obj) {
        assert obj != null;
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof DataFrame : pair.get(0).getClass();
        assert pair.get(1) instanceof List;
        DataFrame df = (DataFrame) pair.get(0);
        List cols = (List) pair.get(1);
        List<String> colArgs = new ArrayList<>();
        for (Object o : cols) {
            Integer index = (Integer) o;
            String arg = df.getNames().get(index);
            colArgs.add(arg);
        }
        assert !colArgs.isEmpty();
        DataFrame res = df.select(colArgs);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        assert false : obj;
        Pair<Boolean, Maybe<Object>> arg = (Pair<Boolean, Maybe<Object>>) obj;
        assert false : arg;
//        DataFrame df = (DataFrame) obj;
//        if ((df.getNcol() <= key) || (df.getNcol() <= value) || (key == value)) {
//            return new Pair<>(false, new Maybe<>());
//        } else {
//            String keyCol = df.getNames().get(key);
//            String valCol = df.getNames().get(value);
//            DataFrame res = ReshapeKt.spread(df, keyCol, valCol, null, false);
//            return new Pair<>(true, new Maybe<>(res));
//        }
        return null;
    }

    public String toString() {
        return "l(x).(select " + " x)";
    }
}
