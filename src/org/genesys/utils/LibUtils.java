package org.genesys.utils;

import com.microsoft.z3.BoolExpr;
import javafx.beans.binding.ObjectExpression;
import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

import java.util.ArrayList;
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
        return System.nanoTime();
    }

    public static AbstractList getAbsList(List arg) {
        LinkedList myList = new LinkedList(arg);
        AbstractList abstractList = construct(myList);
//        System.out.println("convert: " + abstractList);
        return abstractList;
    }

    public static Object fixGsonBug(Object data) {
        if (data instanceof List) {
            List dataList = (List) data;
            List tgtList = new ArrayList();
            for (Object l : dataList) {
                List innerList = new ArrayList();
                if (l instanceof ArrayList) {
                    for (Object elem : (List) l) {
                        if (elem instanceof Double) {
                            innerList.add(((Double) elem).intValue());
                        } else {
                            innerList.add(elem);
                        }
                    }
                    tgtList.add(innerList);
                } else {
                    if (l instanceof Double) {
                        tgtList.add(((Double) l).intValue());
                    } else {
                        tgtList.add(l);
                    }
                }
            }
            return tgtList;
        } else if (data instanceof Double) {
            return ((Double) data).intValue();
        } else {
            return data;
        }
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

    @SuppressWarnings("unchecked")
    public static <T extends List<?>> T cast(Object obj) {
        return (T) obj;
    }
}
