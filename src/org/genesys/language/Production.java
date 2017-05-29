package org.genesys.language;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 5/26/17.
 */
public class Production <T> {

    public final String function;

    public final T[] inputs;

    public final String source;

    public String[] spec;

    @SafeVarargs
    public Production(String src, String function, T ... inputs) {
        this.source = src;
        this.function = function;
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.function);
        if(this.inputs.length > 0) {
            sb.append("(");
            for(T t : this.inputs) {
                sb.append(t.toString()).append(", ");
            }
            sb.delete(sb.length()-2, sb.length());
            sb.append(")");
        }
        return sb.toString();
    }
}
