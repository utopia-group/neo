package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import org.genesys.decide.BaselineSolver;
import org.genesys.decide.AbstractSolver;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.models.Example;
import org.genesys.models.Node;
import org.genesys.models.Problem;
import org.genesys.type.AbstractList;
import org.genesys.type.Maybe;
import org.genesys.utils.LibUtils;

import java.util.List;

/**
 * Created by yufeng on 5/28/17.
 * Default synthesizer for DeepCoder.
 */
public class DefaultSynthesizer implements Synthesizer {

    private AbstractSolver<BoolExpr, Node> solver_;

    private Checker checker_;

    private Interpreter interpreter_;

    private Problem problem_;

    private double totalDecide = 0.0;

    private double totalTest = 0.0;

    public DefaultSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter) {
        solver_ = new BaselineSolver(grammar);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;
    }

    public DefaultSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter, int depth) {
        solver_ = new BaselineSolver(grammar, depth);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;
    }

    @Override
    public Node synthesize() {
                /* retrieve an AST from the solver */
        Node ast = solver_.getModel(null);

        /* do deduction */
        while (!checker_.check(problem_, ast)) {
            ast = solver_.getModel(null);
        }

        while (ast != null) {
            /* check input-output using the interpreter */
            if (verify(ast)) {
                System.out.println("Synthesized PROGRAM: " + ast);
                break;
            }
            long start = LibUtils.tick();
            ast = solver_.getModel(null);
            long end = LibUtils.tick();
            totalDecide += LibUtils.computeTime(start, end);
        }
        System.out.println("Decide time=:" + (totalDecide));
        System.out.println("Test time=:" + (totalTest));

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
                System.out.println("Exception!!!!");
                passed = false;
                break;
            }
        }
        long end = LibUtils.tick();
        totalTest += LibUtils.computeTime(start, end);
        return passed;
    }
}
