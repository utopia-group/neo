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
public class BaseValidatorDriver2 implements ValidatorDriver2<Node, Object> {

    public final Map<String, Validator2> validators = new HashMap<>();

    // nodeId -> result of partial evaluation
    public final Map<Integer, Object> peMap = new HashMap<>();


    @Override
    public Pair<Object, List<Map<Integer, List<String>>>> validate(Node node, Object input) {
        List<Pair<Object, List<Map<Integer, List<String>>>>> arglist = new ArrayList<>();

        if(!node.isConcrete()) return  new Pair<>(true, new ArrayList<>());

        for (Node child : node.children) {
            Pair<Object, List<Map<Integer, List<String>>>> childObj = validate(child, input);
            arglist.add(childObj);
            if(childObj.t0 instanceof Boolean) return  new Pair<>(true, new ArrayList<>());
            if(childObj.t0 == null) return childObj;
        }

        assert arglist.size() == node.children.size();

        Pair<Object, List<Map<Integer, List<String>>>> ret = validators.get(node.function).validate(arglist, input, node);
        if ((ret.t1 != null) && (node.id != 0)) peMap.put(node.id, ret.t1);
        return ret;
    }

    @Override
    public Object getPE(int key) {
        assert peMap.containsKey(key) : key;
        return peMap.get(key);
    }

    @Override
    public void cleanPEMap() {
        peMap.clear();
    }

    public Map<Integer, Object> getPeMap() {
        return this.peMap;
    }
}
