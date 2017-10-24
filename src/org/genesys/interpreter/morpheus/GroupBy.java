package org.genesys.interpreter.morpheus;

import krangl.DataFrame;
import krangl.Extensions;
import org.genesys.interpreter.Unop;
import org.genesys.language.MorpheusGrammar;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;
import org.genesys.utils.LibUtils;

import java.util.*;

/**
 * Created by yufeng on 9/3/17.
 */
public class GroupBy implements Unop {

    public Object apply(Object obj) {
        assert obj != null;
        List pair = (List) obj;
        assert pair.size() == 2 : pair;
        assert pair.get(0) instanceof DataFrame : pair.get(0).getClass();
        assert pair.get(1) instanceof List;
        DataFrame df = (DataFrame) pair.get(0);
        List cols = (List) pair.get(1);
        String[] colArgs = new String[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            Integer index = (Integer) cols.get(i);
            String arg = df.getNames().get(index);
            colArgs[i] = arg;
        }
        assert colArgs.length > 0;
        DataFrame res = df.groupBy(colArgs);
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
            String[] colArgs = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Integer index = (Integer) cols.get(i);
                //don't support negation for now.
                if (index < 0) return new Pair<>(false, new Maybe<>());
                if (nCol <= index) return new Pair<>(false, new Maybe<>());
                String arg = df.getNames().get(index);
                colArgs[i] = arg;
            }
            assert colArgs.length > 0;
            DataFrame res = df.groupBy(colArgs);
            //strange bug in the interpreter
            if (!res.getRows().iterator().hasNext()) return new Pair<>(false, new Maybe<>());
//            System.out.println("groupBy==================");
//            System.out.println(res);
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
        Node fstChild = ast.children.get(0);
        Node sndChild = ast.children.get(1);

        if (conflictList.isEmpty())
            conflictList.add(new HashMap<>());

        List<String> hasCol = MorpheusGrammar.colListMap.get(nCol);
        if (nCol < cols.size() && hasCol != null) {
            for (Map<Integer, List<String>> partialConflictMap : conflictList) {
                //current node.
                partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                //arg0
                partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                //arg1
                partialConflictMap.put(sndChild.id, MorpheusGrammar.colListMap.get(nCol));
            }
            return new Pair<>(null, conflictList);
        } else {
            String[] colArgs = new String[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                Integer index = (Integer) cols.get(i);
                //don't support negation for now.
                if (nCol <= index || index < 0) {
                    List<Map<Integer, List<String>>> conflicts1 = LibUtils.deepClone(conflictList);
                    if (hasCol != null) {
                        for (Map<Integer, List<String>> partialConflictMap : conflicts1) {
                            //current node.
                            partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                            //arg0
                            partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                            //arg1
                            partialConflictMap.put(sndChild.id, MorpheusGrammar.colListMap.get(nCol));
                        }
                    }

                    List<Map<Integer, List<String>>> conflicts2 = LibUtils.deepClone(conflictList);
                    for (Map<Integer, List<String>> partialConflictMap : conflicts2) {
                        //current node.
                        partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                        //arg0
                        partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                        //arg1
                        partialConflictMap.put(sndChild.id, MorpheusGrammar.negColList);
                    }
                    conflicts1.addAll(conflicts2);
                    return new Pair<>(null, conflicts1);
                }
                String arg = df.getNames().get(index);
                colArgs[i] = arg;
            }
            assert colArgs.length > 0;
            DataFrame res = df.groupBy(colArgs);
            for (Map<Integer, List<String>> partialConflictMap : conflictList) {
                //current node.
                partialConflictMap.put(ast.id, Arrays.asList(ast.function));
                //arg0
                partialConflictMap.put(fstChild.id, Arrays.asList(fstChild.function));
                //arg1
                partialConflictMap.put(sndChild.id, Arrays.asList(sndChild.function));
            }
            //strange bug in the interpreter
            return new Pair<>(res, conflictList);
        }

    }

    public String toString() {
        return "l(x).(groupBy " + " x)";
    }
}
