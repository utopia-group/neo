package org.genesys.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yufeng on 5/26/17.
 */
public class Node {

    public String function;

    public List<Node> children = new ArrayList<>();

    private String ctrlVar;

    public Node(String function, List<Node> children) {
        this.function = function;
        this.children = children;
    }

    public Node(Node other) {
        this.function = other.function;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public String getCtrlVar() {
        return ctrlVar;
    }

    public void setCtrlVar(String ctrlVar) {
        this.ctrlVar = ctrlVar;
    }

    public Node(String function) {
        this.function = function;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.children.size() > 0) {
            sb.append("(");
        }
        sb.append(this.function).append(" ");
        for (Node child : this.children) {
            sb.append(child.toString()).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (this.children.size() > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    public String traverseModel(Set<String> models) {
        if (!models.contains(ctrlVar)) return "";

        StringBuilder sb = new StringBuilder();
        if (this.children.size() > 0) {
            sb.append("(");
        }

        sb.append(this.function).append(" ");
        for (Node child : this.children) {
            sb.append(child.traverseModel(models)).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (this.children.size() > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

}
