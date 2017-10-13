package org.genesys.synthesis;

import com.google.gson.Gson;
import com.microsoft.z3.BoolExpr;
import krangl.DataFrame;
import org.genesys.interpreter.MorpheusValidator;
import org.genesys.interpreter.MorpheusValidator2;
import org.genesys.language.MorpheusGrammar;
import org.genesys.models.*;
import org.genesys.type.Maybe;
import org.genesys.utils.LibUtils;
import org.genesys.utils.Z3Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by yufeng on 9/3/17.
 * Deduction for Morpheus
 */
public class MorpheusChecker implements Checker<Problem, List<List<Pair<Integer, List<String>>>>> {

    private HashMap<String, Component> components_ = new HashMap<>();

    private Gson gson = new Gson();

    private MorpheusValidator2 validator_;

    private int counter_ = 0;

    private List<List<Pair<Integer, List<String>>>> core_ = new ArrayList<>();

    public MorpheusChecker(String specLoc, MorpheusGrammar g) throws FileNotFoundException {
        File[] files = new File(specLoc).listFiles();
        for (File file : files) {
            assert file.isFile() : file;
            String json = file.getAbsolutePath();
            Component comp = gson.fromJson(new FileReader(json), Component.class);
            components_.put(comp.getName(), comp);
        }
        validator_ = new MorpheusValidator2(g.getInitProductions());
    }

    /**
     * @param specification: Input-output specs.
     * @param node:          Partial AST from Ruben.
     * @return
     */
    @Override
    public boolean check(Problem specification, Node node) {
        core_.clear();
        Example example = specification.getExamples().get(0);
        Object output = example.getOutput();
        assert output instanceof DataFrame;
        DataFrame outDf = (DataFrame) output;
        List inputs = example.getInput();

        // Perform type-checking and PE.
        validator_.cleanPEMap();
        System.out.println("Verifying.... " + node);
//        validator_.validate(node, example.getInput());
//        if (!validRes.t0) {
//            counter_++;
//            System.out.println("Refuting.... " + node + " " + counter_);
//            return false;
//        } else {
////            System.out.println("Verifying.... " + node);
//        }

        /* Generate SMT formula for current AST node. */
        Queue<Node> queue = new LinkedList<>();
        Z3Utils z3 = Z3Utils.getInstance();
        List<BoolExpr> cstList = new ArrayList<>();
        Map<String, Object> clauseToNodeMap_ = new HashMap<>();

        // Generate constraints from PE.
//        for (int i : validator_.getPeMap().keySet()) {
//            Object o = validator_.getPE(i);
//            if (o instanceof DataFrame) {
//                DataFrame peDf = (DataFrame) o;
//                int peRow = peDf.getNrow();
//                int peCol = peDf.getNcol();
//                String peRowVar = "V_ROW" + i;
//                String peColVar = "V_COL" + i;
//                BoolExpr peRowCst = z3.genEqCst(peRowVar, peRow);
//                BoolExpr peColCst = z3.genEqCst(peColVar, peCol);
//
//                cstList.add(peRowCst);
//                cstList.add(peColCst);
//            }
//        }

        queue.add(node);
        while (!queue.isEmpty()) {
            Node worker = queue.remove();
            //Generate constraint between worker and its children.
            String func = worker.function;
            String colVar = "V_COL" + worker.id;
            String rowVar = "V_ROW" + worker.id;

            //Get component spec.
            Component comp = components_.get(func);
//            System.out.println("working on : " + func + " id:" + worker.id + " isconcrete:" + worker.isConcrete());
            if ("root".equals(func)) {
                //attach output
                int outCol = outDf.getNcol();
                int outRow = outDf.getNrow();

                BoolExpr outColCst = z3.genEqCst(colVar, outCol);
                assert worker.children.size() == 1;
                Node lastChild = worker.children.get(0);
                String childVar = "V_COL" + lastChild.id;
                BoolExpr eqColCst = z3.genEqCst(colVar, childVar);

                cstList.add(outColCst);
                cstList.add(eqColCst);

                BoolExpr outRowCst = z3.genEqCst(rowVar, outRow);
                assert worker.children.size() == 1;
                String childVarRow = "V_ROW" + lastChild.id;
                BoolExpr eqRowCst = z3.genEqCst(rowVar, childVarRow);

                cstList.add(outRowCst);
                cstList.add(eqRowCst);
            } else if (func.contains("input")) {
                //attach inputs
                List<String> nums = LibUtils.extractNums(func);
                assert !nums.isEmpty();
                int index = Integer.valueOf(nums.get(0));
                DataFrame inDf = (DataFrame) inputs.get(index);

                int inCol = inDf.getNcol();
                int inRow = inDf.getNrow();

                BoolExpr inColCst = z3.genEqCst(colVar, inCol);
                cstList.add(inColCst);

                BoolExpr inRowCst = z3.genEqCst(rowVar, inRow);
                cstList.add(inRowCst);
//                // adding input constraint to the core.
                clauseToNodeMap_.put(inColCst.toString(), worker.id);
                clauseToNodeMap_.put(inRowCst.toString(), worker.id);
            } else {
                if (!worker.children.isEmpty()) {
                    if (comp != null) {
                        for (String cstStr : comp.getConstraint()) {

                            if (worker.isConcrete()) {
                                Pair<Object, List<Map<Integer, List<String>>>> validRes = validator_.validate(worker, example.getInput());
                                Object judge = validRes.t0;
                                if (judge == null) {
                                    parseCore(validRes.t1);
                                    System.out.println("prune by type inhabitation: " + worker);
                                    return false;
                                } else if (!(judge instanceof Boolean)) {
                                    DataFrame workerDf = (DataFrame) validRes.t0;
                                    int peRow = workerDf.getNrow();
                                    int peCol = workerDf.getNcol();
                                    if (peRow == 0 || peCol == 0)
                                        return false;
                                    String peRowVar = "V_ROW" + worker.id;
                                    String peColVar = "V_COL" + worker.id;
                                    BoolExpr peRowCst = z3.genEqCst(peRowVar, peRow);
                                    BoolExpr peColCst = z3.genEqCst(peColVar, peCol);
                                    cstList.add(peRowCst);
                                    cstList.add(peColCst);
                                    List<Pair<Integer, List<String>>> folComp = new ArrayList<>();
                                    for (int c = 1; c < worker.children.size(); c++) {
                                        Node folChild = worker.children.get(c);
                                        folComp.add(new Pair<>(folChild.id, Arrays.asList(folChild.function)));
                                    }
//                                    System.out.println("current node for PE: " + worker);
                                    clauseToNodeMap_.put(peRowCst.toString(), folComp);
                                    clauseToNodeMap_.put(peColCst.toString(), folComp);

//                                    System.out.println("current PE constraint:" + peColCst);
                                }
                            }
                            String targetCst = cstStr.replace("RO_SPEC", rowVar);
                            targetCst = targetCst.replace("CO_SPEC", colVar);

                            for (int i = 0; i < worker.children.size(); i++) {
                                Node child = worker.children.get(i);
                                String colChildVar = "V_COL" + child.id;

                                String targetId = "CI" + i + "_SPEC";
                                targetCst = targetCst.replace(targetId, colChildVar);

                                String rowChildVar = "V_ROW" + child.id;
                                String targetId2 = "RI" + i + "_SPEC";
                                targetCst = targetCst.replace(targetId2, rowChildVar);
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
                queue.add(child);
            }
        }

        boolean sat = z3.isSat(cstList, clauseToNodeMap_, components_.values());
        if (!sat) System.out.println("Prune program:" + node);
        return sat;
    }

    @Override
    public List<List<Pair<Integer, List<String>>>> learnCore() {
        return core_;
    }

    private void parseCore(List<Map<Integer, List<String>>> coreList) {
        for (Map<Integer, List<String>> conflict : coreList) {
            List<Pair<Integer, List<String>>> c = new ArrayList<>();
            for (int key : conflict.keySet()) {
                c.add(new Pair<>(key, conflict.get(key)));
            }
            core_.add(c);
        }
    }
}
