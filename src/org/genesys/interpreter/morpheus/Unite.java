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
public class Unite implements Unop {

    private String sep_ = "_";

    private boolean remove = false;

    private int lhs;

    private int rhs;

    public Unite(int l, int r) {
        lhs = l;
        rhs = r;
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

    public String toString() {
        return "l(x).(unite " + " x)";
    }
}
