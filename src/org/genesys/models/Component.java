package org.genesys.models;

import java.util.List;

/**
 * Created by yufeng on 7/7/17.
 */
public class Component {

    @Override
    public String toString() {
        return "Component{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", constraint=" + constraint +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getConstraint() {
        return constraint;
    }

    private Integer id;

    private String name;

    private List<String> constraint;

}
