package org.genesys.synthesis;

import com.google.gson.Gson;
import com.microsoft.z3.BoolExpr;
import krangl.DataFrame;
import org.apache.commons.lang3.StringUtils;
import org.genesys.interpreter.MorpheusValidator2;
import org.genesys.language.MorpheusGrammar;
import org.genesys.models.*;
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

    private List<List<Pair<Integer, List<String>>>> core_ = new ArrayList<>();

    private Map<String, Object> clauseToNodeMap_ = new HashMap<>();
    //Map a clause to its original spec
    private Map<String, String> clauseToSpecMap_ = new HashMap<>();

    private String[] spec1 = {"CO_SPEC", "RO_SPEC", "CI0_SPEC", "RI0_SPEC", "CI1_SPEC", "RI1_SPEC"};

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
        /* Generate SMT formula for current AST node. */
        Queue<Node> queue = new LinkedList<>();
        Z3Utils z3 = Z3Utils.getInstance();
        List<BoolExpr> cstList = new ArrayList<>();


        queue.add(node);
        while (!queue.isEmpty()) {
            Node worker = queue.remove();
            //Generate constraint between worker and its children.
            String func = worker.function;

            //Get component spec.
            Component comp = components_.get(func);
//            System.out.println("working on : " + func + " id:" + worker.id + " isconcrete:" + worker.isConcrete());
            if ("root".equals(func)) {
                List<BoolExpr> abs = abstractTable(worker, outDf);
                List<BoolExpr> align = alignOutput(worker);
                cstList.addAll(abs);
                cstList.addAll(align);
            } else if (func.contains("input")) {
                //attach inputs
                List<String> nums = LibUtils.extractNums(func);
                assert !nums.isEmpty();
                int index = Integer.valueOf(nums.get(0));
                DataFrame inDf = (DataFrame) inputs.get(index);

                List<BoolExpr> abs = abstractTable(worker, inDf);
                cstList.addAll(abs);
            } else {
                if (!worker.children.isEmpty() && comp != null) {
                    if (worker.isConcrete()) {
                        long start2 = LibUtils.tick();
                        Pair<Object, List<Map<Integer, List<String>>>> validRes = validator_.validate(worker, example.getInput());
                        long end2 = LibUtils.tick();
                        MorpheusSynthesizer.typeinhabit += LibUtils.computeTime(start2, end2);

                        Object judge = validRes.t0;
                        if (judge == null) {
                            parseCore(validRes.t1);
                            System.out.println("prune by type inhabitation: " + worker);
                            return false;
                        } else if (!(judge instanceof Boolean)) {
                            DataFrame workerDf = (DataFrame) validRes.t0;
                            if (isValid(workerDf)) {
                                parseCore((validRes.t1));
                                return false;
                            }
                            List<BoolExpr> abs = abstractTable(worker, workerDf);
                            cstList.addAll(abs);
                        }
                    }
                    List<BoolExpr> nodeCst = genNodeSpec(worker, comp);
                    cstList.addAll(nodeCst);
                }
            }

            for (int i = 0; i < worker.children.size(); i++) {
                Node child = worker.children.get(i);
                queue.add(child);
            }
        }

        boolean sat = z3.isSat(cstList, clauseToNodeMap_, clauseToSpecMap_, components_.values());
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

    // Given an AST, generate clause for its current assignments
    private List<Pair<Integer, List<String>>> getCurrentAssignment(Node node) {
        List<Pair<Integer, List<String>>> clauses = new ArrayList<>();
        Pair<Integer, List<String>> worker = new Pair<>(node.id, Arrays.asList(node.function));
        clauses.add(worker);
        for (Node child : node.children) {
            clauses.addAll(getCurrentAssignment(child));
        }
        return clauses;
    }

    private List<BoolExpr> genNodeSpec(Node worker, Component comp) {
        //{"CO_SPEC", "RO_SPEC", "CI0_SPEC", "RI0_SPEC", "CI1_SPEC", "RI1_SPEC"};
        String[] dest = new String[6];
        String colVar = "V_COL" + worker.id;
        String rowVar = "V_ROW" + worker.id;
        dest[0] = colVar;
        dest[1] = rowVar;
        Node child0 = worker.children.get(0);
        String colChild0Var = "V_COL" + child0.id;
        String rowChild0Var = "V_ROW" + child0.id;
        dest[2] = colChild0Var;
        dest[3] = rowChild0Var;
        String colChild1Var = "#";
        String rowChild1Var = "#";
        if (worker.children.size() > 1) {
            Node child1 = worker.children.get(1);
            colChild1Var = "V_COL" + child1.id;
            rowChild1Var = "V_ROW" + child1.id;
        }
        dest[4] = colChild1Var;
        dest[5] = rowChild1Var;
        List<BoolExpr> cstList = new ArrayList<>();

        for (String cstStr : comp.getConstraint()) {
            String targetCst = StringUtils.replaceEach(cstStr, spec1, dest);
            BoolExpr expr = Z3Utils.getInstance().convertStrToExpr(targetCst);
            cstList.add(expr);
            clauseToNodeMap_.put(expr.toString(), worker.id);
            clauseToSpecMap_.put(expr.toString(), cstStr);
        }

        return cstList;
    }

    private List<BoolExpr> abstractTable(Node worker, DataFrame df) {
        List<BoolExpr> cstList = new ArrayList<>();
        int row = df.getNrow();
        int col = df.getNcol();
        String rowVar = "V_ROW" + worker.id;
        String colVar = "V_COL" + worker.id;
        BoolExpr rowCst = Z3Utils.getInstance().genEqCst(rowVar, row);
        BoolExpr colCst = Z3Utils.getInstance().genEqCst(colVar, col);

        cstList.add(rowCst);
        cstList.add(colCst);

        if ("root".equals(worker.function) || worker.function.contains("input")) {
            clauseToNodeMap_.put(rowCst.toString(), worker.id);
            clauseToNodeMap_.put(colCst.toString(), worker.id);
        } else {
            List<Pair<Integer, List<String>>> currAssigns = getCurrentAssignment(worker);
            clauseToNodeMap_.put(rowCst.toString(), currAssigns);
            clauseToNodeMap_.put(colCst.toString(), currAssigns);
        }
        return cstList;
    }

    private boolean isValid(DataFrame df) {
        return (df.getNcol() == 0 || df.getNcol() == 0);
    }

    private List<BoolExpr> alignOutput(Node worker) {
        List<BoolExpr> cstList = new ArrayList<>();
        String colVar = "V_COL" + worker.id;
        String rowVar = "V_ROW" + worker.id;
        assert worker.children.size() == 1;
        Node lastChild = worker.children.get(0);
        String childVar = "V_COL" + lastChild.id;
        BoolExpr eqColCst = Z3Utils.getInstance().genEqCst(colVar, childVar);
        String childVarRow = "V_ROW" + lastChild.id;
        BoolExpr eqRowCst = Z3Utils.getInstance().genEqCst(rowVar, childVarRow);
        cstList.add(eqRowCst);
        cstList.add(eqColCst);
        return cstList;
    }
}
