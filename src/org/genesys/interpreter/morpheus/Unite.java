package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
import org.genesys.utils.MorpheusUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Unite implements Unop {

    private String sep_ = "_";

    private boolean remove = false;

    private int lhs;

    private int rhs;

    public Unite(int l, int r) {
        lhs = l;
        rhs = r;
    }

    public Unite() {
    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        DataFrame df = (DataFrame) obj;
        assert df.getNames().size() > lhs;
        assert df.getNames().size() > rhs;
        String lhsCol = df.getNames().get(lhs);
        String rhsCol = df.getNames().get(rhs);
        List<String> colList = new ArrayList<>();
        colList.add(lhsCol);
        colList.add(rhsCol);
        String colName = MorpheusUtil.getInstance().getMorpheusString();
        DataFrame res = ReshapeKt.unite(df, colName, colList, sep_, remove);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);
        Pair<Boolean, Maybe<Object>> arg2 = args.get(2);

        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        int lhs = (int) arg1.t1.get();
        int rhs = (int) arg2.t1.get();
        int nCol = df.getNcol();
        if ((nCol <= lhs) || (nCol <= rhs) || (lhs == rhs)) return new Pair<>(false, new Maybe<>());
        String lhsCol = df.getNames().get(lhs);
        String rhsCol = df.getNames().get(rhs);
        List<String> colList = new ArrayList<>();
        colList.add(lhsCol);
        colList.add(rhsCol);
        String colName = MorpheusUtil.getInstance().getMorpheusString();
        DataFrame res = ReshapeKt.unite(df, colName, colList, sep_, remove);
        return new Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(unite " + " x)";
    }
}
