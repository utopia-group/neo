package org.genesys.utils;

import com.microsoft.z3.BoolExpr;
import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

import java.util.LinkedList;
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

    public static AbstractList getAbsList(List arg) {
        LinkedList myList = new LinkedList(arg);
        AbstractList abstractList = construct(myList);
//        System.out.println("convert: " + abstractList);
        return abstractList;
    }

    /* recursively construct cons */
    private static AbstractList construct(LinkedList arg) {
        if (arg.isEmpty())
            return new EmptyList();
        else {
            Object fst = arg.pollFirst();
            //FIXME: The stupid bug in Gson.
            if (fst instanceof Double) fst = ((Double) fst).intValue();
            return new Cons(fst, construct(arg));
        }
    }
}
