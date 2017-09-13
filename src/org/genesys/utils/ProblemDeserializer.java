package org.genesys.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.genesys.models.Problem;

import java.lang.reflect.Type;

/**
 * Created by yufeng on 9/12/17.
 */
public class ProblemDeserializer implements JsonDeserializer<Problem> {
    @Override
    public Problem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        assert false;

        return null;
    }
}
