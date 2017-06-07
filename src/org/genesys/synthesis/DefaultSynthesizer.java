package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import org.genesys.decide.BaselineSolver;
import org.genesys.decide.AbstractSolver;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.models.Example;
import org.genesys.models.Node;
import org.genesys.type.AbstractList;
import org.genesys.type.Cons;
import org.genesys.type.EmptyList;

/**
 * Created by yufeng on 5/28/17.
 */
public class DefaultSynthesizer implements Synthesizer {

    private AbstractSolver<BoolExpr, Node> solver_;

    private Checker checker_;

    private Interpreter interpreter_;

    private Example problem_;

    public DefaultSynthesizer(Grammar grammar, Example problem, Checker checker, Interpreter interpreter) {
        solver_ = new BaselineSolver(grammar);
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

        /* check input-output using the interpreter */
        while (ast != null) {
            AbstractList list = new Cons(0, new Cons(1, new Cons(2, new EmptyList())));
//            Node actualAst = ast;
            System.out.println("PROGRAM: " + ast);
            ast = solver_.getModel(null);
            //        System.out.println("INPUT: " + list);
//        System.out.println("OUTPUT: " + interpreter_.execute(actualAst, list).get());
        }

        return ast;
    }
}
