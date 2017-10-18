package org.genesys.utils;

import com.microsoft.z3.*;

import java.util.*;

import org.genesys.models.Component;
import org.genesys.models.Pair;
import org.genesys.synthesis.MorpheusSynthesizer;


/**
 * Created by yufeng on 5/26/17.
 */
public class Z3Utils {

    private static Z3Utils instance = null;

    private Context ctx_;

    private Context ctx_core;

    private Solver solver_;

    private Solver solver_core;

    private Model model_;

    private boolean unSatCore_ = true;

    private int cstCnt_ = 1;

    /* Global counter for z3 var. */
    private int counter = 0;

    private Map<String, BoolExpr> stringBoolExprMap;

    private Map<BoolExpr, BoolExpr> cstMap_ = new HashMap<>();

    private List<Pair<Integer, List<String>>> conflicts_ = new ArrayList<>();

    protected Z3Utils() {
        ctx_ = new Context();
        ctx_core = new Context();

        solver_core = ctx_core.mkSolver();
        solver_ = ctx_.mkSolver();
        stringBoolExprMap = new HashMap<>();
    }

    public static Z3Utils getInstance() {
        if (instance == null) {
            instance = new Z3Utils();
        }
        return instance;
    }

    public void reset() {
        counter = 0;
        stringBoolExprMap.clear();
    }

    public BoolExpr getFreshBoolVar() {
        String var = "bool_" + counter;
        BoolExpr pa = ctx_.mkBoolConst(var);
        stringBoolExprMap.put(var, pa);
        counter++;
        return pa;
    }

    public BoolExpr genVarByName(String var) {
        BoolExpr pa = ctx_.mkBoolConst(var);
        return pa;
    }

    public BoolExpr genEqCst(String var, int val) {
        BoolExpr eq = ctx_.mkEq(ctx_.mkIntConst(var), ctx_.mkInt(val));
        return eq;
    }

    public BoolExpr genEqCst(String var, String val) {
        BoolExpr eq = ctx_.mkEq(ctx_.mkIntConst(var), ctx_.mkIntConst(val));
        return eq;
    }

    public BoolExpr getVarById(String id) {
        return stringBoolExprMap.get(id);
    }

    public BoolExpr conjoin(BoolExpr... exprs) {
        if (exprs.length == 0) return this.trueExpr();
        BoolExpr pa = ctx_.mkAnd(exprs);
        return pa;
    }

    public BoolExpr neg(BoolExpr expr) {
        return ctx_.mkNot(expr);
    }

    public BoolExpr imply(BoolExpr a, BoolExpr b) {
        BoolExpr pa = ctx_.mkImplies(a, b);
        return pa;
    }

    public BoolExpr trueExpr() {
        return ctx_.mkTrue();
    }

    public BoolExpr disjoin(BoolExpr... exprs) {
        if (exprs.length == 1) return exprs[0];
        BoolExpr pa = ctx_.mkOr(exprs);
        return pa;
    }

    public BoolExpr exactOne(BoolExpr... exprs) {
        /* Exact one var will be assigned true. */
        if (exprs.length == 1) return exprs[0];
        List<BoolExpr> list = new ArrayList<>();

        for (int i = 0; i < (exprs.length - 1); i++) {
            BoolExpr fst = exprs[i];
            for (int j = (i + 1); j < (exprs.length); j++) {
                BoolExpr snd = exprs[j];
                BoolExpr exactOne = ctx_.mkNot(ctx_.mkAnd(fst, snd));
                list.add(exactOne);
            }
        }
        list.add(disjoin(exprs));
        BoolExpr[] array = new BoolExpr[list.size()];
        array = list.toArray(array);

        return ctx_.mkAnd(array);
    }

    public void init(BoolExpr expr) {
        solver_.reset();
        solver_.add(expr);
    }

    public Model getModel() {
        if (solver_.check() == Status.SATISFIABLE) {
            Model m = solver_.getModel();
            model_ = m;
        } else {
            model_ = null;
        }
        return model_;
    }

    public boolean blockSolution() {
        List<BoolExpr> current = new ArrayList<>();
        for (FuncDecl fd : model_.getConstDecls()) {
            Expr val = model_.getConstInterp(fd);
            if (val.equals(this.trueExpr())) {
                BoolExpr varExpr = stringBoolExprMap.get(fd.getName().toString());
                current.add(varExpr);
            }
        }
        BoolExpr[] currArray = new BoolExpr[current.size()];
        currArray = current.toArray(currArray);
        BoolExpr currModel = this.conjoin(currArray);
//        System.out.println("block: " + currModel);
        solver_.add(ctx_.mkNot(currModel));
        return (solver_.check() == Status.SATISFIABLE);
    }

    public BoolExpr convertStrToExpr(String cst) {
        BoolExpr e = ctx_.parseSMTLIB2String(cst, null, null, null, null);
        return e;
    }

    public BoolExpr convertStrToExpr2(String cst) {
        BoolExpr e = ctx_core.parseSMTLIB2String(cst, null, null, null, null);
        return e;
    }

    public boolean isSat(List<BoolExpr> exprList, Map<String, Object> clauseToNodeMap, Map<String, String> clauseToSpecMap_, Collection<Component> components) {
        long start2 = LibUtils.tick();
        solver_.push();
        for (BoolExpr expr : exprList) {
//            solver_.add(expr);
            this.addCst(expr);
        }
        boolean flag = (solver_.check() == Status.SATISFIABLE);
        if (!flag && unSatCore_) {
            printUnsatCore(clauseToNodeMap, clauseToSpecMap_, components);
        } else {
            conflicts_.clear();
        }
        solver_.pop();
        cstCnt_ = 1;
        long end2 = LibUtils.tick();
        MorpheusSynthesizer.smt1 += LibUtils.computeTime(start2, end2);
        return flag;
    }

    public void addCst(BoolExpr e) {
        if (unSatCore_) {
            int val = cstCnt_;
            BoolExpr p1 = ctx_.mkBoolConst(Integer.toString(val));
            solver_.assertAndTrack(e, p1);
            cstMap_.put(p1, e);
            cstCnt_++;
        } else {
            solver_.add(e);
        }
    }

    public void printUnsatCore(Map<String, Object> clauseToNodeMap, Map<String, String> clauseToSpecMap_, Collection<Component> components) {
        long start2 = LibUtils.tick();
//        System.out.println("UNSAT_core===========:" + solver_.getUnsatCore().length);
        conflicts_.clear();
        for (BoolExpr e : solver_.getUnsatCore()) {
            String core = cstMap_.get(e).toString();
//            System.out.println(e + " " + cstMap_.get(e) + " *****" + clauseToNodeMap.containsKey(core));
            // only consider useful core for now.
            if (clauseToNodeMap.containsKey(core)) {
//                System.out.println("***" + core);
                int nodeId;
                Object nodeObj = clauseToNodeMap.get(core);
                if (nodeObj instanceof Integer)
                    nodeId = (Integer) nodeObj;
                else {
                    List<Pair<Integer, List<String>>> folComp = (List<Pair<Integer, List<String>>>) nodeObj;
                    conflicts_.addAll(folComp);
                    continue;
                }
                if (!clauseToSpecMap_.containsKey(core)) continue;
                String core_str = clauseToSpecMap_.get(core);
                BoolExpr my_core = convertStrToExpr2(core_str);

                List<String> eq_vec = new ArrayList<>();
                for (Component comp : components) {
                    List<BoolExpr> expr_vector = new ArrayList<>();
                    solver_core.push();
                    for (String cst : comp.getConstraint()) {
                        BoolExpr comCst = convertStrToExpr2(cst);
                        expr_vector.add(comCst);
                        solver_core.add(comCst);
                    }
                    solver_core.add(ctx_core.mkNot(my_core));
                    boolean flag = (solver_core.check() == Status.SATISFIABLE);
                    solver_core.pop();
                    if (!flag && !eq_vec.contains(comp.getId())) {
                        eq_vec.add(comp.getName());
//                        System.out.println("Checking cmp: " + comp.getName() + " CST:" + expr_vector);
                    }
                }
                Pair<Integer, List<String>> conflict = new Pair<>(nodeId, eq_vec);
                if (!conflicts_.contains(conflict)) conflicts_.add(conflict);
            }
        }
        long end2 = LibUtils.tick();
        MorpheusSynthesizer.smt2 += LibUtils.computeTime(start2, end2);
    }

    public List<Pair<Integer, List<String>>> getConflicts() {
        return conflicts_;
    }
}
