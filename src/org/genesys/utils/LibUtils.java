package org.genesys.utils;

import com.microsoft.z3.BoolExpr;

import java.util.List;

/**
 * Created by yufeng on 5/29/17.
 */
public class LibUtils {

    /**
     * Convert list to array.
     * FIXME:How to implement the generic version?
     *
     * @param list
     * @param <T>
     * @return array
     */
    public static BoolExpr[] listToArray(List<BoolExpr> list) {
        BoolExpr[] array = new BoolExpr[list.size()];
        array = list.toArray(array);
        return array;
    }

    public static double computeTime(long start, long end) {
        double diff = end - start;
        return (diff / 1e6);
    }

    public static long tick() {
        return System.currentTimeMillis();
    }
}
