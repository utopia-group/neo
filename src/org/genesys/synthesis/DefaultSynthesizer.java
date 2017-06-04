package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import org.genesys.decide.BaselineSolver;
import org.genesys.decide.ISolver;
import org.genesys.decide.SATSolver;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.models.Example;
import org.genesys.utils.LibUtils;
import org.genesys.models.Node;
import org.genesys.models.Trio;
import org.genesys.utils.Z3Utils;

import java.util.*;

/**
 * Created by yufeng on 5/28/17.
 */
public class DefaultSynthesizer implements Synthesizer {

    private ISolver<BoolExpr, Node> solver_;

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
        Node ast = (Node) solver_.getModel(null);

        /* do deduction */
        while (!checker_.check(problem_, ast)) {
            ast = solver_.getModel(null);
        }

        /* check input-output using the interpreter */
//        interpreter_.execute(ast);
        return ast;
    }
}
