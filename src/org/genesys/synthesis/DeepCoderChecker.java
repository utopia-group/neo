package org.genesys.synthesis;

import com.google.gson.Gson;
import com.microsoft.z3.BoolExpr;
import org.genesys.models.Component;
import org.genesys.models.Node;
import org.genesys.models.Problem;
import org.genesys.utils.Z3Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by yufeng on 6/3/17.
 */
public class DeepCoderChecker implements Checker<Problem, BoolExpr> {

    private HashMap<String, Component> components_ = new HashMap<>();

    private Gson gson = new Gson();


    public DeepCoderChecker(String specLoc) throws FileNotFoundException {
        File[] files = new File(specLoc).listFiles();
        for (File file : files) {
            assert file.isFile() : file;
            String json = file.getAbsolutePath();
            Component comp = gson.fromJson(new FileReader(json), Component.class);
            components_.put(comp.getName(), comp);
        }
    }

    /**
     * @param specification: Input-output specs.
     * @param node:          Partial AST from Ruben.
     * @return
     */
    @Override
    public boolean check(Problem specification, Node node) {
        System.out.println("checking=========" + node);
        /* Generate SMT formula for current AST node. */
        Queue<Node> queue = new LinkedList<>();
        Z3Utils z3 = Z3Utils.getInstance();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node worker = queue.remove();
            //Generate constraint between worker and its children.
            String func = worker.function;
            Component comp = components_.get(func);
            System.out.println("component:" + func + " " + comp);
            if (comp != null) {
                for (String cst : comp.getConstraint()) {
                    BoolExpr expr = z3.convertStrToExpr(cst);
                }
            }

            for (Node child : worker.children) {
                queue.add(child);
            }
        }
        return true;
    }

    @Override
    public BoolExpr learnCore() {
        return null;
    }
}
