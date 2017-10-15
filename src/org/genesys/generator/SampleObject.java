package org.genesys.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by utcs on 10/14/17.
 */
public class SampleObject {

    private String name;
    private List<Example> examples = new ArrayList<>();

    public SampleObject(String name, List<List<Object>> inputs, List<Object> outputs){
        for (int i = 0; i < inputs.size(); i++){
            Example ex = new Example(inputs.get(i), outputs.get(i));
            examples.add(ex);
        }
        this.name = name;
    }

}

