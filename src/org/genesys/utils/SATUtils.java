package org.genesys.utils;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.MixedDataStructureDanielWL;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.orders.RSATPhaseSelectionStrategy;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.restarts.Glucose21Restarts;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.TimeoutException;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by utcs on 7/7/17.
 */
public class SATUtils {

    private static SATUtils instance = null;

    private List<Boolean> variablesOccurs = new ArrayList<>();

    private Solver solver_ = null;

    private int nbVars = 0;

    private boolean init = false;

    public void createSolver() {
        MiniSATLearning<DataStructureFactory> learning = new MiniSATLearning<DataStructureFactory>();
        Solver<DataStructureFactory> solver = new Solver<DataStructureFactory>(
                learning, new MixedDataStructureDanielWL(), new VarOrderHeap(), new MiniSATRestarts());
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        solver.setOrder(new VarOrderHeap(new RSATPhaseSelectionStrategy()));
        solver.setRestartStrategy(new Glucose21Restarts());
        solver.setLearnedConstraintsDeletionStrategy(solver.glucose);
        solver.setTimeout(36000);
        solver_ = solver;
    }

    public void createVars(int vars) {
//        for (int i = 0; i < vars; i++)
//            variablesOccurs.add(false);
        solver_.newVar(vars);

//        nbVars = vars;
//
//        // SAT4J requires that a variable exists in the formula; otherwise it may lead to problems
//        // hack to circunvent this problem -- uses a dummy variable
//        for (int i = 1; i <= vars; i++) {
//            boolean res = addClause(new VecInt(new int[]{i, vars + 1}));
//        }
//
//        try {
//            // hack to initialize SAT4J data structures
//            solver_.isSatisfiable();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }
    }

    public Constr propagate(){
        if (!init) {
            try {
                // hack to initialize SAT4J data structures
                boolean res = solver_.initNeo();
                assert (res);
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            init = true;
        }

        return solver_.propagate();
    }

    public int getNbVars() {
        return nbVars;
    }

    public boolean varOccurs(int var) {
        assert (var < variablesOccurs.size());
        return variablesOccurs.get(var);
    }

    public Solver getSolver() {
        return solver_;
    }


    public boolean blockTrail(List<Integer> trail) {
        assert (solver_ != null);
        assert (!trail.isEmpty());

        VecInt clause = new VecInt();
        for (int i = 0; i < trail.size(); i++) {
            clause.push(-trail.get(i));
        }
        return addClause(clause);
    }

    public boolean allVariablesAssigned() {
        assert (solver_ != null);

        boolean ok = true;
        for (int i = 0; i <= solver_.nVars(); i++) {
            if (solver_.truthValue(posLit(i)) == Lbool.UNDEFINED) {
                ok = false;
                break;
            }
        }
        return ok;
    }

    public boolean addClause(VecInt clause) {
        assert (solver_ != null);

//        for (int i = 0; i < clause.size(); i++){
//            System.out.print(clause.get(i) + " ");
//        }
//        System.out.println("0");

        boolean conflict = false;
        try {
            solver_.addClause(clause);
        } catch (ContradictionException e) {
            conflict = true;
        }
        return conflict;
    }

    public int posLit(int var) {
        return var << 1;
    }

    public int negLit(int var) {
        return var << 1 ^ 1;
    }

    public static SATUtils getInstance() {
        if (instance == null) {
            instance = new SATUtils();
        }
        return instance;
    }

    public boolean addAMO(VecInt lits) {

        assert (lits.size() != 0);
        boolean conflict = false;

        if (lits.size() == 1) {
            if (addClause(lits))
                conflict = true;
        } else {

            VecInt seqAuxiliary = new VecInt();
            int vars = solver_.nVars();
            solver_.newVar(solver_.nVars() + lits.size());

            for (int i = 0; i < lits.size() - 1; i++) {
                seqAuxiliary.push(++vars);
            }

            for (int i = 0; i < lits.size(); i++) {
                if (i == 0) {
                    // Uncomment this if you want an EO encoding
//                    if (addClause(new VecInt(new int[]{lits.get(i), -seqAuxiliary.get(i)})))
//                        conflict = true;
                    if (addClause(new VecInt(new int[]{-lits.get(i), seqAuxiliary.get(i)})))
                        conflict = true;
                } else if (i == lits.size() - 1) {
                    if (addClause(new VecInt(new int[]{lits.get(i), seqAuxiliary.get(i - 1)})))
                        conflict = true;
                    if (conflict = addClause(new VecInt(new int[]{-lits.get(i), -seqAuxiliary.get(i - 1)})))
                        conflict = true;
                } else {
                    if (addClause(new VecInt(new int[]{-seqAuxiliary.get(i - 1), seqAuxiliary.get(i)})))
                        conflict = true;
                    if (addClause(new VecInt(new int[]{lits.get(i), -seqAuxiliary.get(i), seqAuxiliary.get(i - 1)})))
                        conflict = true;
                    if (addClause(new VecInt(new int[]{-lits.get(i), seqAuxiliary.get(i)})))
                        conflict = true;
                    if (addClause(new VecInt(new int[]{-lits.get(i), -seqAuxiliary.get(i - 1)})))
                        conflict = true;
                }
            }
        }
        return conflict;
    }

    public int analyzeSATConflict(Constr conflict) {
        System.out.println("conflict= " + conflict.toString());
        System.out.println("trail= " + solver_.trail.toString());
        org.sat4j.minisat.core.Pair analysisResult = new org.sat4j.minisat.core.Pair();
        int backjumpLevel = -1;
        int rootLevel = 0;

        if (solver_.decisionLevel() == rootLevel) {
            // unsat
            return backjumpLevel;
        } else {
            int conflictTrailLevel = solver_.trail.size();
            // analyze conflict
            try {
                solver_.analyze(conflict, analysisResult);
            } catch (TimeoutException e) {
                // we should never have a timeout
            }
            assert analysisResult.backtrackLevel < solver_.decisionLevel();
            backjumpLevel = Math.max(analysisResult.backtrackLevel, rootLevel);
            solver_.cancelUntil(backjumpLevel);
            assert solver_.decisionLevel() >= rootLevel
                    && solver_.decisionLevel() >= analysisResult.backtrackLevel;

            if (analysisResult.reason == null) {
                backjumpLevel = -1;
                return backjumpLevel;
            }

            System.out.println("backjumplevel= " + backjumpLevel);
            assert (analysisResult.reason != null);

            System.out.println("clause learnt= " + analysisResult.reason.toString());
            solver_.record(analysisResult.reason);
            analysisResult.reason = null;
        }
        return backjumpLevel;
    }


}
