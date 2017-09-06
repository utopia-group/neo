package org.genesys.interpreter;

import org.genesys.interpreter.morpheus.*;
import org.genesys.models.Node;
import org.genesys.type.Maybe;

import java.util.*;

/**
 * interpreter for Morpheus.
 * Created by yufeng on 9/3/17.
 */
public class MorpheusInterpreter extends BaseInterpreter {

    public MorpheusInterpreter() {
        executors.put("root", (objects, input) -> {
            Object obj = objects.get(0);
            if (obj instanceof Unop)
                return new Maybe<>(((Unop) objects.get(0)).apply(input));
            else
                return new Maybe<>(obj);
        });
        executors.put("input0", (objects, input) -> new Maybe<>(((List) input).get(0)));
        executors.put("input1", (objects, input) -> new Maybe<>(((List) input).get(1)));
        executors.put("true", (objects, input) -> new Maybe<>(true));
        executors.put("false", (objects, input) -> new Maybe<>(false));

        executors.put("select", (objects, input) -> {
            List args = new ArrayList();
            args.add(objects.get(0));
            args.add(objects.get(1));
            return new Maybe<>(new Select().apply(args));
        });


        executors.put("gather", (objects, input) -> {
            List args = new ArrayList();
            args.add(objects.get(0));
            args.add(objects.get(1));
            return new Maybe<>(new Gather().apply(args));
        });

        executors.put("spread", (objects, input) -> {
            assert objects.size() == 3;
            int key = (int) objects.get(1);
            int value = (int) objects.get(2);
            return new Maybe<>(new Spread(key, value).apply(objects.get(0)));
        });

        executors.put("unite", (objects, input) -> {
            assert objects.size() == 3;
            int lhs = (int) objects.get(1);
            int rhs = (int) objects.get(2);
            return new Maybe<>(new Unite(lhs, rhs).apply(objects.get(0)));
        });

        executors.put("separate", (objects, input) -> {
            assert objects.size() == 2;
            int orgCol = (int) objects.get(1);
            return new Maybe<>(new Separate(orgCol).apply(objects.get(0)));
        });

        executors.put("summarise", (objects, input) -> {
            assert objects.size() == 3;
            String aggr = (String) objects.get(1);
            int colIdx = (int) objects.get(2);
            return new Maybe<>(new Summarise(aggr, colIdx).apply(objects.get(0)));
        });

        executors.put("group_by", (objects, input) -> {
            List args = new ArrayList();
            args.add(objects.get(0));
            args.add(objects.get(1));
            return new Maybe<>(new GroupBy().apply(args));
        });

        executors.put("inner_join", (objects, input) -> {
            List args = new ArrayList();
            args.add(objects.get(0));
            args.add(objects.get(1));
            return new Maybe<>(new InnerJoin().apply(args));
        });

        executors.put("mutate", (objects, input) -> {
            assert objects.size() == 4;
            int lhs = (int) objects.get(1);
            Binop op = (Binop) objects.get(2);
            int rhs = (int) objects.get(3);
            return new Maybe<>(new Mutate(lhs, op, rhs).apply(objects.get(0)));
        });

        //TODO
        executors.put("filter", (objects, input) -> {
            assert objects.size() == 4;
            Binop op = (Binop) objects.get(1);
            int colIdx = (int) objects.get(2);
            int val = (int) objects.get(3);
            return new Maybe<>(new org.genesys.interpreter.morpheus.Filter(op, colIdx, val).apply(objects.get(0)));
        });

        executors.put("l(a,b).(/ a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("/")));
        executors.put("l(a,b).(> a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop(">")));
        executors.put("l(a,b).(< a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("<")));
        executors.put("l(a,b).(== a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("==")));
        executors.put("l(a,b).(|| a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("||")));
        executors.put("l(a,b).(&& a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("&&")));
        executors.put("l(a).(+ a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("+", objects.get(0))));
        executors.put("l(a).(* a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("*", objects.get(0))));
        executors.put("l(a).(> a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop(">", objects.get(0))));
        executors.put("l(a).(< a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("<", objects.get(0))));
        executors.put("l(a).(== a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("==", objects.get(0))));

        //FIXME: should be generated dynamically
        executors.put("0", (objects, input) -> new Maybe<>(0));
        executors.put("1", (objects, input) -> new Maybe<>(1));
        executors.put("1", (objects, input) -> new Maybe<>(1));
        List<Integer> arg = new ArrayList<>();
        arg.add(1);
        arg.add(2);
        arg.add(3);
        executors.put("[1, 2, 3]", (objects, input) -> new Maybe<>(arg));
    }
}
