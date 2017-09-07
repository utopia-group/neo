package org.genesys.interpreter;

import org.genesys.models.Node;
import org.genesys.type.Maybe;

import java.util.*;

/**
 * Created by yufeng on 5/30/17.
 */
public class BaseInterpreter implements Interpreter<Node, Object> {

    public final Map<String, Executor> executors = new HashMap<>();

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
        assert arglist.size() == node.children.size();

        return this.executors.get(node.function).execute(arglist, input);
    }

    @Override
    public Set<String> getExeKeys() {
        throw new UnsupportedOperationException("Unsupported interpreter: Default.");
    }
}
