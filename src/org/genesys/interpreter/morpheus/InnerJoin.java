package org.genesys.interpreter.morpheus;

import kotlin.Pair;
import krangl.DataFrame;
import krangl.JoinsKt;
import org.genesys.interpreter.Unop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class InnerJoin implements Unop {

    public Object apply(Object obj) {
        assert obj != null;
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof DataFrame;
        assert pair.get(1) instanceof DataFrame;
        DataFrame df = (DataFrame) pair.get(0);
        DataFrame df2 = (DataFrame) pair.get(1);
        List<String> commons = new ArrayList<>(df.getNames());
        commons.retainAll(df2.getNames());
        DataFrame res = JoinsKt.innerJoin(df, df2, commons, new Pair<>("",""));
        return res;
    }


    public String toString() {
        return "l(x).(select " + " x)";
    }
}
