package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import org.genesys.interpreter.Unop;

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

    public String toString() {
        return "l(x).(select " + " x)";
    }
}
