package org.genesys.interpreter;

import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yufeng on 9/10/17.
 */
public class BaseValidatorDriver implements ValidatorDriver<Node, Object> {

    public final Map<String, Validator> validators = new HashMap<>();

    // nodeId -> result of partial evaluation
    public final Map<String, Object> peMap = new HashMap<>();

    @Override
    public Pair<Boolean, Maybe<Object>> validate(Node node, Object input) {
        List<Pair<Boolean, Maybe<Object>>> arglist = new ArrayList<>();

        if (!node.isConcrete()) return new Pair<>(true, new Maybe());

        for (Node child : node.children) {
            Pair<Boolean, Maybe<Object>> childObj = validate(child, input);
            boolean childFlag = childObj.t0;
            if (!childFlag) return new Pair<>(false, new Maybe<>());

            arglist.add(childObj);
        }

        assert validators.containsKey(node.function) : "Invalid argument." + node.function;
        assert arglist.size() == node.children.size();

        Pair<Boolean, Maybe<Object>> ret = validators.get(node.function).validate(arglist, input);
        return ret;
    }

    @Override
    public Object getPE(String key) {
        assert peMap.containsKey(key) : key;
        return peMap.get(key);
    }

    @Override
    public void cleanPEMap() {
        peMap.clear();
    }
}
