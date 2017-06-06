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
        Node ast2 = solver_.getModel(null);
        Node ast3 = solver_.getModel(null);
        Node ast4 = solver_.getModel(null);
        Node ast5 = solver_.getModel(null);
        Node ast6 = solver_.getModel(null);
        Node ast7 = solver_.getModel(null);
        Node ast8 = solver_.getModel(null);
        Node ast9 = solver_.getModel(null);
        Node ast10 = solver_.getModel(null);

        System.out.println("ast" + ast);
        System.out.println("ast2" + ast2);
        System.out.println("ast3" + ast3);
        System.out.println("ast4" + ast4);
        System.out.println("ast5" + ast5);
        System.out.println("ast6" + ast6);
        System.out.println("ast7" + ast7);
        System.out.println("ast8" + ast8);
        System.out.println("ast9" + ast9);
        System.out.println("ast10" + ast10);

        /* check input-output using the interpreter */
        AbstractList list = new Cons(0, new Cons(1, new Cons(2, new EmptyList())));
        Node actualAst = ast3.children.get(0);
        System.out.println("PROGRAM: " + actualAst);
        System.out.println("INPUT: " + list);
        System.out.println("OUTPUT: " + interpreter_.execute(actualAst, list).get());


        return ast;
    }
}
