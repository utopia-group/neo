package org.genesys.synthesis;

import com.google.gson.Gson;
import com.microsoft.z3.BoolExpr;
import org.genesys.models.*;
import org.genesys.utils.LibUtils;
import org.genesys.utils.Z3Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by yufeng on 6/3/17.
 */
public class DeepCoderChecker implements Checker<Problem, List<Pair<Integer, List<Integer>>>> {

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

        Example example = specification.getExamples().get(0);
        Object output = example.getOutput();
        List inputs = example.getInput();

        /* Generate SMT formula for current AST node. */
        Queue<Node> queue = new LinkedList<>();
        Z3Utils z3 = Z3Utils.getInstance();
        List<BoolExpr> cstList = new ArrayList<>();
        Map<String, Integer> clauseToNodeMap_ = new HashMap<>();

        queue.add(node);
        while (!queue.isEmpty()) {
            Node worker = queue.remove();
            //Generate constraint between worker and its children.
            String func = worker.function;
            String workerVar = "V_LEN" + worker.id;
            String workerVar2 = "V_MAX" + worker.id;
            //Get component spec.
            Component comp = components_.get(func);

            // only uses the max constraints if all children of zipwith and map are present
            // FIXME: fix this hack in the future
            boolean hackMax = ((!func.equals("ZIPWITH") && !func.equals("MAP")) ||
                    (func.equals("ZIPWITH") && worker.children.size() == 3) ||
                    (func.equals("MAP") && worker.children.size() == 2));

            if ("root".equals(func)) {
                //attach output
                int outSize = 1;
                int outMax;
                if (output instanceof List) {
                    //len
                    outSize = ((List) output).size();

                    //max
                    assert output instanceof List : output;
                    List<Double> list = LibUtils.cast(output);
                    Optional<Double> max = list.stream().reduce(Double::max);
                    outMax = max.get().intValue();
                } else {
                    outMax = ((Double) output).intValue();
                }

                BoolExpr outCst = z3.genEqCst(workerVar, outSize);
                assert worker.children.size() == 1;
                Node lastChild = worker.children.get(0);
                String childVar = "V_LEN" + lastChild.id;
                BoolExpr eqCst = z3.genEqCst(workerVar, childVar);

                cstList.add(outCst);
                cstList.add(eqCst);


                if (hackMax){
                    BoolExpr outMaxCst = z3.genEqCst(workerVar2, outMax);
                    assert worker.children.size() == 1;
                    String childVar2 = "V_MAX" + lastChild.id;
                    BoolExpr eqCst2 = z3.genEqCst(workerVar2, childVar2);

                    cstList.add(outMaxCst);
                    cstList.add(eqCst2);
                }

            } else if (func.contains("input")) {
                //attach inputs
                List<String> nums = LibUtils.extractNums(func);
                assert !nums.isEmpty();
                int index = Integer.valueOf(nums.get(0));
                Object inputObj = inputs.get(index);
                int inSize = 1;
                int inMax;
                if (inputObj instanceof List) {
                    inSize = ((List) inputObj).size();

                    assert inputObj instanceof List : inputObj;
                    List<Double> list = LibUtils.cast(inputObj);
                    Optional<Double> max = list.stream().reduce(Double::max);
                    inMax = max.get().intValue();
                } else {
                    inMax = ((Double) inputObj).intValue();
                }
                BoolExpr inCst = z3.genEqCst(workerVar, inSize);
                cstList.add(inCst);

                BoolExpr inMaxCst = z3.genEqCst(workerVar2, inMax);
                cstList.add(inMaxCst);
                // adding input constraint to the core.
                clauseToNodeMap_.put(inCst.toString(), worker.id);
                clauseToNodeMap_.put(inMaxCst.toString(), worker.id);

            } else {
                if (!worker.children.isEmpty()) {
                    if (comp != null) {
                        for (String cstStr : comp.getConstraint()) {
                            String targetCst = cstStr.replace("OUT_LEN_SPEC", workerVar);
                            if (hackMax) {
                                targetCst = targetCst.replace("OUT_MAX_SPEC", workerVar2);
                            }

                            for (int i = 0; i < worker.children.size(); i++) {
                                Node child = worker.children.get(i);
                                String childVar = "V_LEN" + child.id;

                                String targetId = "IN" + i + "_LEN_SPEC";
                                targetCst = targetCst.replace(targetId, childVar);

                                if (hackMax) {
                                    String childVar2 = "V_MAX" + child.id;
                                    String targetId2 = "IN" + i + "_MAX_SPEC";
                                    targetCst = targetCst.replace(targetId2, childVar2);
                                }
                            }

                            Node fstChild = worker.children.get(0);
                            Component fstComp = components_.get(fstChild.function);
                            if (fstComp != null && comp.isHigh()) {
                                List<String> childSpecs = fstComp.getConstraint();
                                for (String childCst : childSpecs) {
                                    if (hackMax) {
                                        String fstChildVar = "V_MAX" + fstChild.id;
                                        childCst = childCst.replace("OUT_MAX_SPEC", fstChildVar);
                                        String postfix = "_" + worker.id;
                                        childCst = childCst.replaceAll("IN[0-9]_MAX_SPEC", "$0" + postfix);
                                        BoolExpr exprChild = z3.convertStrToExpr(childCst);
                                        cstList.add(exprChild);
                                        clauseToNodeMap_.put(exprChild.toString(), fstChild.id);
                                    }
                                }
                            }

                            if (hackMax) {
                                if (targetCst.contains("IN0_ARG")) {
                                    targetCst = targetCst.replace("0_ARG", "");
                                    targetCst = targetCst.replace("CHILD", String.valueOf(worker.id));
                                }
                            }

                                BoolExpr expr = z3.convertStrToExpr(targetCst);
                                cstList.add(expr);
                                clauseToNodeMap_.put(expr.toString(), worker.id);
                        }
                    }
                }
            }

            for (int i = 0; i < worker.children.size(); i++) {
                Node child = worker.children.get(i);
                //FIXME: this code is ugly!!!
                if ((comp != null) && comp.isHigh() && (i == 0) && !child.toString().contains("input")) continue;
                queue.add(child);
            }
        }
//        for (BoolExpr b : cstList){
//            System.out.println(b);
//        }
        boolean sat = z3.isSat(cstList, clauseToNodeMap_, components_.values());
        if (!sat) System.out.println("Prune program:" + node);

        return sat;
    }

    @Override
    public List<Pair<Integer, List<Integer>>> learnCore() {
        Z3Utils z3 = Z3Utils.getInstance();
        return z3.getConflicts();
    }
}
