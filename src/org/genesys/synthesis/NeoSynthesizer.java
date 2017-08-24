package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import org.genesys.decide.AbstractSolver;
import org.genesys.decide.NeoSolver;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.models.Example;
import org.genesys.models.Node;
import org.genesys.models.Problem;
import org.genesys.type.Maybe;
import org.genesys.utils.LibUtils;
import org.genesys.utils.Z3Utils;

/**
 * Created by ruben on 7/6/17.
 */
public class NeoSynthesizer implements Synthesizer {

    private AbstractSolver<BoolExpr, Node> solver_;

    private Checker checker_;

    private Interpreter interpreter_;

    private Problem problem_;

    private double totalDecide = 0.0;

    private double totalTest = 0.0;

    public NeoSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter) {
        solver_ = new NeoSolver(grammar);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;
    }

    public NeoSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter, int depth) {
        solver_ = new NeoSolver(grammar, depth);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;
    }

    @Override
    public Node synthesize() {

        /* retrieve an AST from the solver */
        Node ast = solver_.getModel(null);
        int total = 0;
        int prune = 0;

        while (ast != null) {
            /* do deduction */
            total++;
            if (!checker_.check(problem_, ast)) {
                long start = LibUtils.tick();
//                Z3Utils z3 = Z3Utils.getInstance();
//                z3.getConflicts();
                ast = solver_.getModel(null);
                long end = LibUtils.tick();
                prune++;
                totalDecide += LibUtils.computeTime(start, end);
                continue;
            }

            /* check input-output using the interpreter */
            if (verify(ast)) {
                System.out.println("Synthesized PROGRAM: " + ast);
                break;
            } else {
                long start = LibUtils.tick();
                ast = solver_.getModel(null);
                long end = LibUtils.tick();
                totalDecide += LibUtils.computeTime(start, end);
            }
        }
        System.out.println("Decide time=:" + (totalDecide));
        System.out.println("Test time=:" + (totalTest));
        System.out.println("total: " + total + " prune:" + prune + " %:" + (prune * 100.0)/total);

        return ast;
    }

    /* Verify the program using I-O examples. */
    private boolean verify(Node program) {
        long start = LibUtils.tick();
        boolean passed = true;
        System.out.println("Program: " + program);
        for (Example example : problem_.getExamples()) {
            //FIXME:lets assume we only have at most two input tables for now.
            Object input = LibUtils.fixGsonBug(example.getInput());
            // Always one output table
            Object output = LibUtils.fixGsonBug(example.getOutput());
            try {
                Maybe<Object> tgt = interpreter_.execute(program, input);

                if (!tgt.get().equals(output)) {
                    passed = false;
                    break;
                }
            } catch (Exception e) {
                System.out.println("Exception= " + e);
                passed = false;
                break;
            }
        }
        long end = LibUtils.tick();
        totalTest += LibUtils.computeTime(start, end);
        return passed;
    }


}
