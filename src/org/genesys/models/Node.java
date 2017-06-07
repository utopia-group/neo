package org.genesys.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by yufeng on 5/26/17.
 */
public class Node {

    private Object symbol;

    public String function;

    public List<Node> children = new ArrayList<>();

    public Node() {
    }

    public Node(String function, List<Node> children) {
        this.function = function;
        this.children = children;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public Node(String function) {
        this.function = function;
    }

    public Object getSymbol() {
        return symbol;
    }

    public void setSymbol(Object symbol) {
        this.symbol = symbol;
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

}
