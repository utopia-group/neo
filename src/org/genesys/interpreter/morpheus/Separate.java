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
public class Separate implements Unop {

    private final String sep_ = "_";

    private final boolean remove_ = false;

    private final boolean convert_ = false;

    private int colVal;

    public  Separate(int v) {
        colVal = v;
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

    public String toString() {
        return "l(x).(separate " + " x)";
    }
}
