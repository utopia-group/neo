package org.genesys.utils;

import com.microsoft.z3.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yufeng on 5/26/17.
 */
public class Z3Utils {

    private static Z3Utils instance = null;

    private Context ctx_;

    private Solver solver_;

    private Model model_;

    /* Global counter for z3 var. */
    private int counter = 0;

    private Map<String, BoolExpr> stringBoolExprMap;

    protected Z3Utils() {
        ctx_ = new Context();
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

    public BoolExpr conjoin(BoolExpr... exprs) {
        if (exprs.length == 0) return this.trueExpr();
        BoolExpr pa = ctx_.mkAnd(exprs);
        return pa;
    }

    public BoolExpr imply(BoolExpr a, BoolExpr b) {
        BoolExpr pa = ctx_.mkImplies(a, b);
        return pa;
    }

    public BoolExpr trueExpr() {
        return ctx_.mkTrue();
    }

    public BoolExpr disjoin(BoolExpr... exprs) {
        BoolExpr pa = ctx_.mkOr(exprs);
        return pa;
    }

    public BoolExpr exactOne(BoolExpr... exprs) {
        /* Exact one var will be assigned true. */
        if (exprs.length == 1) return exprs[0];
        List<BoolExpr> list = new ArrayList<>();

        for (int i = 0; i < (exprs.length - 1); i++) {
            BoolExpr fst = exprs[i];
            for (int j = 1; j < (exprs.length); j++) {
                BoolExpr snd = exprs[j];
                BoolExpr exactOne = ctx_.mkXor(fst, snd);
                list.add(exactOne);
            }
        }
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
            System.out.println("UNSAT_core:" + solver_.getUnsatCore().length);
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
}
