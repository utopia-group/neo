package org.genesys.interpreter.morpheus;

import kotlin.Pair;
import krangl.DataFrame;
import krangl.Extensions;
import krangl.JoinsKt;
import org.genesys.interpreter.Unop;
import org.genesys.type.Maybe;

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
        DataFrame res = JoinsKt.innerJoin(df, df2, commons, new Pair<>("", ""));
        return res;
    }

    public org.genesys.models.Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<org.genesys.models.Pair<Boolean, Maybe<Object>>> args = (List<org.genesys.models.Pair<Boolean, Maybe<Object>>>) obj;
        org.genesys.models.Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        org.genesys.models.Pair<Boolean, Maybe<Object>> arg1 = args.get(1);

        if (!arg0.t1.has() || !arg1.t1.has()) return new org.genesys.models.Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        DataFrame df2 = (DataFrame) arg1.t1.get();
        if ((df.getNcol() == 0) || (df2.getNcol() == 0) || (df2.getNrow() == 0) || (df.getNrow() == 0))
            return new org.genesys.models.Pair<>(false, new Maybe<>());

        List<String> commons = new ArrayList<>(df.getNames());
        commons.retainAll(df2.getNames());
        if(commons.isEmpty()) return new org.genesys.models.Pair<>(false, new Maybe<>());
        DataFrame res = JoinsKt.innerJoin(df, df2, commons, new Pair<>("", ""));
//        System.out.println("InnerJoin-------------");
//        Extensions.print(res);
        return new org.genesys.models.Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(select " + " x)";
    }
}
