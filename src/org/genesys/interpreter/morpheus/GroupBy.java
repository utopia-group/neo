package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class GroupBy implements Unop {

    public Object apply(Object obj) {
        assert obj != null;
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof DataFrame : pair.get(0).getClass();
        assert pair.get(1) instanceof List;
        DataFrame df = (DataFrame) pair.get(0);
        List cols = (List) pair.get(1);
        String[] colArgs = new String[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            Integer index = (Integer) cols.get(i);
            String arg = df.getNames().get(index);
            colArgs[i] = arg;
        }
        assert colArgs.length > 0;
        DataFrame res = df.groupBy(colArgs);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);
        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());
        DataFrame df = (DataFrame) arg0.t1.get();
        List cols = (List) arg1.t1.get();

        int nCol = df.getNcol();
        if (nCol <= cols.size()) {
            return new Pair<>(false, new Maybe<>());
        } else {
            String[] colArgs = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Integer index = (Integer) cols.get(i);
                //don't support negation for now.
                if(index < 0) return new Pair<>(false, new Maybe<>());
                if (nCol <= index) return new Pair<>(false, new Maybe<>());
                String arg = df.getNames().get(index);
                colArgs[i] = arg;
            }
            assert colArgs.length > 0;
            DataFrame res = df.groupBy(colArgs);
            //strange bug in the interpreter
            if(!res.getRows().iterator().hasNext()) return new Pair<>(false, new Maybe<>());
            return new Pair<>(true, new Maybe<>(res));
        }
    }

    public String toString() {
        return "l(x).(select " + " x)";
    }
}
