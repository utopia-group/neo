package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.ReshapeKt;
import krangl.StringCol;
import org.genesys.interpreter.Unop;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
import org.genesys.utils.MorpheusUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/3/17.
 */
public class Separate implements Unop {

    private final String sep_ = "_";

    private final boolean remove_ = false;

    private final boolean convert_ = false;

    private int colVal;

    public Separate(int v) {
        colVal = v;
    }

    public Separate() {
    }

    public Object apply(Object obj) {
        assert obj instanceof DataFrame;
        DataFrame df = (DataFrame) obj;
        List<String> colArgs = new ArrayList<>();
        String col1 = MorpheusUtil.getInstance().getMorpheusString();
        String col2 = MorpheusUtil.getInstance().getMorpheusString();
        String orgCol = df.getNames().get(colVal);
        colArgs.add(col1);
        colArgs.add(col2);

        DataFrame res = ReshapeKt.separate(df, orgCol, colArgs, sep_, remove_, convert_);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);

        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        int colIdx = (int) arg1.t1.get();
        if ((df.getNcol() <= colIdx) || (df.getNrow() == 0)) return new Pair<>(false, new Maybe<>());
        if (!(df.getCols().get(colIdx) instanceof StringCol)) return new Pair<>(false, new Maybe<>());
        List<String> colArgs = new ArrayList<>();
        String col1 = MorpheusUtil.getInstance().getMorpheusString();
        String col2 = MorpheusUtil.getInstance().getMorpheusString();
        String orgCol = df.getNames().get(colIdx);
        colArgs.add(col1);
        colArgs.add(col2);
        StringCol strCol = (StringCol) df.getCols().get(colIdx);
        String testVal = strCol.getValues()[0];
        if (testVal == null || !testVal.contains("_")) return new Pair<>(false, new Maybe<>());
        DataFrame res = ReshapeKt.separate(df, orgCol, colArgs, sep_, remove_, convert_);
        return new Pair<>(true, new Maybe<>(res));
    }

    public String toString() {
        return "l(x).(separate " + " x)";
    }
}
