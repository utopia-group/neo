package org.genesys.interpreter;

import org.genesys.interpreter.deepcode.FilterLList;
import org.genesys.interpreter.deepcode.MapLList;
import org.genesys.interpreter.deepcode.MaximumUnop;
import org.genesys.interpreter.deepcode.ZipWith;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.*;

/**
 * interpreter for L2 tool. Can be used in Deepcoder
 * Created by yufeng on 5/31/17.
 */
public class DeepCoderInterpreter implements Interpreter<Node, Object> {

    private final Map<String, Executor> executors = new HashMap<String, Executor>();

    public DeepCoderInterpreter() {
        executors.put("apply_to_input", (objects, input) -> new Maybe<>(((Unop) objects.get(0)).apply(input)));
        executors.put("true", (objects, input) -> new Maybe<>(true));
        executors.put("false", (objects, input) -> new Maybe<>(false));
        executors.put("0", (objects, input) -> new Maybe<>(0));
        executors.put("1", (objects, input) -> new Maybe<>(1));
        executors.put("+1", (objects, input) -> new Maybe<>((int) objects.get(0) + 1));
        executors.put("*3", (objects, input) -> new Maybe<>((int) objects.get(0) * 3));
        executors.put("maximum", (objects, input) -> new Maybe<>(new MaximumUnop()));
        executors.put("-", (objects, input) -> new Maybe<>(-(int) objects.get(0)));
        executors.put("emp", (objects, input) -> new Maybe<>(new LinkedList<>()));
        executors.put("pair", (objects, input) -> new Maybe<>(new Pair(objects.get(0), objects.get(1))));
        executors.put("map", (objects, input) -> new Maybe<>(new MapLList((Unop) objects.get(0))));
        executors.put("filter", (objects, input) -> new Maybe<>(new FilterLList((Unop) objects.get(0))));
        executors.put("zipWith", (objects, input) -> new Maybe<>(new ZipWith((Binop) objects.get(0))));
        executors.put("l(a,b).(+ a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("+")));
        executors.put("l(a,b).(* a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("*")));
        executors.put("l(a,b).(% a b)", (objects, input) -> new Maybe<>(new PrimitiveBinop("%")));
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
        executors.put("l(a).(|| a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("||", objects.get(0))));
        executors.put("l(a).(&& a b)", (objects, input) -> new Maybe<>(new PrimitiveUnop("&&", objects.get(0))));
        executors.put("l(a).(- a)", (objects, input) -> new Maybe<>(new PrimitiveUnop("-", null)));
        executors.put("l(a).(~ a)", (objects, input) -> new Maybe<>(new PrimitiveUnop("~", null)));
    }


    @Override
    public Maybe<Object> execute(Node node, Object input) {
        List<Object> arglist = new ArrayList<>();

        for (Node child : node.children) {
            Maybe<Object> object = this.execute(child, input);
            if (!object.has()) {
                return object;
            }
            arglist.add(object.get());
        }

        if (!this.executors.containsKey(node.function)) {
            throw new UnsupportedOperationException("Invalid argument." + node.function);
        }

        return this.executors.get(node.function).execute(arglist, input);
    }
}
