package org.genesys.Interpreter;

import org.genesys.Type.Maybe;

import java.util.List;

/**
 * Created by yufeng on 5/31/17.
 */
public interface Executor {

    Maybe<Object> execute(List<Object> objects, Object input);

}
