package org.genesys.interpreter;

import org.genesys.interpreter.morpheus.Select;
import org.genesys.interpreter.morpheus.Spread;
import org.genesys.language.Production;
import org.genesys.models.Pair;
import org.genesys.type.AbstractType;
import org.genesys.type.Maybe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 9/10/17.
 * The validator will do both type checking and partial evaluation.
 */
public class MorpheusValidator extends BaseValidatorDriver {

    public MorpheusValidator(List<Production<AbstractType>> inits) {

        validators.put("root", (objects, input) -> {
            Object obj = objects.get(0);
            assert obj instanceof Pair : objects;
//            if (obj instanceof Unop)
//                return new Maybe<>(((Unop) objects.get(0)).apply(input));
//            else
//                return new Maybe<>(obj);
            return (Pair) obj;
        });

        validators.put("input0", (objects, input) -> new Pair<>(true, new Maybe<>(((List) input).get(0))));
        validators.put("input1", (objects, input) -> new Pair<>(true, new Maybe<>(((List) input).get(1))));

        validators.put("spread", (objects, input) -> {
            assert objects.size() == 3;
            assert false: objects;
            return null;
//            int key = (int) objects.get(1);
//            int value = (int) objects.get(2);
//            return new Spread(key, value).verify(objects.get(0));
        });

        validators.put("spread", (objects, input) -> {
            assert objects.size() == 3;
            assert false: objects;
            return null;
//            int key = (int) objects.get(1);
//            int value = (int) objects.get(2);
//            return new Spread(key, value).verify(objects.get(0));
        });

        validators.put("select", (objects, input) -> {
//            return new Maybe<>(new Select().apply(args));
            return new Select().verify(objects);
        });

        for(Production<AbstractType> prod : inits) {
            validators.put(prod.function, (objects, input) -> new Pair<>(true, new Maybe<>(prod.getValue())));
        }
    }

}
