package org.genesys.interpreter.morpheus;

import kotlin.jvm.functions.Function1;
import krangl.DataFrame;
import krangl.Extensions;
import krangl.ReshapeKt;
import org.genesys.interpreter.Unop;
import org.genesys.language.MorpheusGrammar;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
import org.genesys.utils.MorpheusUtil;

import java.util.*;

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
        List<Function1> colNegs = new ArrayList<>();
        boolean hasNeg = false;

        for (Object o : cols) {
            Integer index = (Integer) o;
            int absIdx = index;
            if (index == -99) absIdx = 0;
            String arg = df.getNames().get(Math.abs(absIdx));
            colArgs.add(arg);
            if (index < 0) {
                hasNeg = true;
                colNegs.add(Extensions.unaryMinus(arg));
            }
        }
        assert !colArgs.isEmpty();
        String key = MorpheusUtil.getInstance().getMorpheusString();
        String value = MorpheusUtil.getInstance().getMorpheusString();

        DataFrame res;
        if (hasNeg) {
            Function1[] argNegs = colNegs.toArray(new Function1[colNegs.size()]);
            res = ReshapeKt.gather(df, key, value, argNegs, false);
        } else {
            res = ReshapeKt.gather(df, key, value, colArgs, false);
        }
//        System.out.println("----------------Gather------------------");
//        Extensions.print(df);
//        Extensions.print(res);
        return res;
    }

    public Pair<Boolean, Maybe<Object>> verify(Object obj) {
        List<Pair<Boolean, Maybe<Object>>> args = (List<Pair<Boolean, Maybe<Object>>>) obj;
        Pair<Boolean, Maybe<Object>> arg0 = args.get(0);
        Pair<Boolean, Maybe<Object>> arg1 = args.get(1);

        if (!arg0.t1.has()) return new Pair<>(true, new Maybe<>());

        DataFrame df = (DataFrame) arg0.t1.get();
        List cols = (List) arg1.t1.get();
        int nCol = df.getNcol();
        if (nCol <= cols.size()) {
            return new Pair<>(false, new Maybe<>());
        } else {
            List<String> colArgs = new ArrayList<>();
            List<Function1> colNegs = new ArrayList<>();
            boolean hasNeg = false;

            for (Object o : cols) {
                Integer index = (Integer) o;
                int absIndx = index;
                if (index == -99) absIndx = 0;

                if (nCol <= Math.abs(absIndx)) {
                    return new Pair<>(false, new Maybe<>());
                }
                String arg = df.getNames().get(Math.abs(absIndx));
                colArgs.add(arg);
                if (index < 0) {
                    hasNeg = true;
                    colNegs.add(Extensions.unaryMinus(arg));
                }
            }
            assert !colArgs.isEmpty();
            String key = MorpheusUtil.getInstance().getMorpheusString();
            String value = MorpheusUtil.getInstance().getMorpheusString();
            DataFrame res;
            if (hasNeg) {
                Function1[] argNegs = colNegs.toArray(new Function1[colNegs.size()]);
//                System.out.println("Running gather...." + argNegs) ;

                res = ReshapeKt.gather(df, key, value, argNegs, false);
            } else {
                res = ReshapeKt.gather(df, key, value, colArgs, false);
            }
//            System.out.println("Gather--------------" + cols);
//            Extensions.print(df);
//            Extensions.print(res);
            return new Pair<>(true, new Maybe<>(res));
        }
    }

    public Pair<Object, List<Map<Integer, List<String>>>> verify2(Object obj, Node ast) {
        List<Pair<Object, List<Map<Integer, List<String>>>>> args = (List<Pair<Object, List<Map<Integer, List<String>>>>>) obj;
        Pair<Object, List<Map<Integer, List<String>>>> arg0 = args.get(0);
        Pair<Object, List<Map<Integer, List<String>>>> arg1 = args.get(1);
        List<Map<Integer, List<String>>> conflictList = arg0.t1;

        DataFrame df = (DataFrame) arg0.t0;
        List cols = (List) arg1.t0;
        int nCol = df.getNcol();

//        System.out.println("Gather--------------" + cols);
//        Extensions.print(df);

        if (conflictList.isEmpty())
            conflictList.add(new HashMap<>());


        if (nCol <= cols.size()) {
            for (Map<Integer, List<String>> partialConflictMap : conflictList) {
                //current node.
                partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                //arg0
                Node fstChild = ast.children.get(0);
                partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                //arg1
                Node sndChild = ast.children.get(1);
                partialConflictMap.put(sndChild.id, MorpheusGrammar.colListMap.get(nCol));
            }
            return new Pair<>(null, conflictList);
        } else {
            List<String> colArgs = new ArrayList<>();
            List<Function1> colNegs = new ArrayList<>();
            boolean hasNeg = false;

            for (Object o : cols) {
                Integer index = (Integer) o;
                int absIndx = index;
                if (index == -99) absIndx = 0;

                if (nCol <= Math.abs(absIndx)) {
                    for (Map<Integer, List<String>> partialConflictMap : conflictList) {
                        //current node.
                        partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                        //arg0
                        Node fstChild = ast.children.get(0);
                        partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                        //arg1
                        Node sndChild = ast.children.get(1);
                        partialConflictMap.put(sndChild.id, MorpheusGrammar.colListMap.get(nCol));
                    }
                    return new Pair<>(null, conflictList);
                }
                String arg = df.getNames().get(Math.abs(absIndx));
                colArgs.add(arg);
                if (index < 0) {
                    hasNeg = true;
                    colNegs.add(Extensions.unaryMinus(arg));
                }
            }
            assert !colArgs.isEmpty();
            String key = MorpheusUtil.getInstance().getMorpheusString();
            String value = MorpheusUtil.getInstance().getMorpheusString();
            for (Map<Integer, List<String>> partialConflictMap : conflictList) {
                //current node.
                partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                //arg0
                Node fstChild = ast.children.get(0);
                partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));

                //arg1
                Node sndChild = ast.children.get(1);
                partialConflictMap.put(sndChild.id, Arrays.asList(sndChild.function));
            }
            DataFrame res;
            if (hasNeg) {
                Function1[] argNegs = colNegs.toArray(new Function1[colNegs.size()]);
//                System.out.println("Running gather...." + argNegs) ;

                res = ReshapeKt.gather(df, key, value, argNegs, false);
            } else {
                res = ReshapeKt.gather(df, key, value, colArgs, false);
            }
//            Extensions.print(res);
            return new Pair<>(res, conflictList);
        }
    }


    public String toString() {
        return "l(x).(gather " + " x)";
    }
}
