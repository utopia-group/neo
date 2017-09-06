package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
import org.genesys.interpreter.Unop;
import org.genesys.utils.MorpheusUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Gather implements Unop {

    public Object apply(Object obj) {
        assert obj instanceof List;
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof DataFrame;
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
        String key = MorpheusUtil.getInstance().getMorpheusString();
        String value = MorpheusUtil.getInstance().getMorpheusString();
        DataFrame res = ReshapeKt.gather(df, key, value, colArgs, false);
        return res;
    }

    public String toString() {
        return "l(x).(gather " + " x)";
    }
}
