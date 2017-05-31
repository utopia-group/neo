package org.genesys.synthesis;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import org.genesys.decide.Decider;
import org.genesys.decide.SATDecider;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.utils.LibUtils;
import org.genesys.models.Node;
import org.genesys.models.Trio;
import org.genesys.utils.Z3Utils;

import java.util.*;

/**
 * Created by yufeng on 5/28/17.
 */
public class DefaultSynthesizer implements Synthesizer {

    private Z3Utils z3Utils;

    private Grammar grammar_;

    private int maxLen_ = 3;

    private final Map<String, Production> ctrlVarMap = new HashMap<>();

    private final Map<String, Node> ctrlVarAstMap = new HashMap<>();

    private Node astRoot;

    private Decider decider;

    public DefaultSynthesizer() {
        z3Utils = Z3Utils.getInstance();
        decider = new SATDecider();
    }

    @Override
    public <T, S> Node synthesize(Grammar<T> grammar, S problem, Checker<S> checker, Interpreter... interpreter) {
        grammar_ = grammar;
        T start = grammar.start();
        astRoot = new Node("root");
        astRoot.setCtrlVar("true");
        ctrlVarAstMap.put("true", astRoot);
        Trio<Integer, BoolExpr, List<BoolExpr>> formula = generate(grammar, start, z3Utils.trueExpr(), maxLen_);
        System.out.println("Big formula: " + formula.t1);
        z3Utils.init(formula.t1);
        Model m = z3Utils.getModel();
        if (m != null) {
            Node ast = translate(m);
            return ast;
        } else {
            return null;
        }
    }

    @Override
    public Node nextSolution() {
        /* block current model */
        boolean hasNext = z3Utils.blockSolution();
        if (hasNext) {
            Model nextModel = z3Utils.getModel();
            Node ast = translate(nextModel);
            return ast;
        } else {
            return null;
        }
    }

    /**
     * Translate Z3 model into a concrete AST node.
     *
     * @param m: Z3 model.
     * @return AST node
     */
    private Node translate(Model m) {
        System.out.println("begin to translate: ");
        Set<String> models = new HashSet<>();
        models.add("true");
        for (FuncDecl fd : m.getConstDecls()) {
            Expr val = m.getConstInterp(fd);
            if (val.equals(z3Utils.trueExpr())) {

                Production prod = ctrlVarMap.get(fd.getName().toString());
                models.add(fd.getName().toString());
//                System.out.println(fd.getName() + " :" + prod.source + " " + prod);
            }
        }

        System.out.println("program: " + astRoot.traverseModel(models));
        return astRoot;
    }

    private <T> Trio<Integer, BoolExpr, List<BoolExpr>> generate(Grammar grammar, T s, BoolExpr parent, int len) {
        if ((len == 0)) {
            return null;
        }
        len--;

        List<Production<T>> prods = grammar.productionsFor(s);
        List<BoolExpr> exactList = new ArrayList<>();
        List<BoolExpr> conjoinList = new ArrayList<>();
        List<BoolExpr> varList = new ArrayList<>();
        String parentVar = parent.toString();

        for (Production<T> prod : prods) {
            BoolExpr var = z3Utils.getFreshBoolVar();
            ctrlVarMap.put(var.toString(), prod);
            Node node = new Node(prod.function);
            assert (ctrlVarAstMap.containsKey(parentVar));
            Node parentNode = ctrlVarAstMap.get(parentVar);
            parentNode.addChild(node);
            ctrlVarAstMap.put(var.toString(), node);
            node.setCtrlVar(var.toString());
//            System.out.println(var + " mapsto: " + prod);
            varList.add(var);
            /* create a fresh var for each production. */
            for (T child : prod.inputs) {
                Trio<Integer, BoolExpr, List<BoolExpr>> subResult = generate(grammar, child, var, len);
                if (subResult == null) continue;

                for (BoolExpr subVar : subResult.t2) {
                    /* if child happens, that implies parent also happens. */
                    conjoinList.add(z3Utils.imply(subVar, var));
                }
                /* Conjoin children's constraints. */
                BoolExpr subExpr = subResult.t1;
                conjoinList.add(subExpr);
            }
            exactList.add(var);
        }

        /* Only one production can happen. */
        BoolExpr[] exactArray = LibUtils.listToArray(exactList);

        /* conjoin current constraints with the children. */
        BoolExpr[] conjoinArray = LibUtils.listToArray(conjoinList);

        BoolExpr exactExpr = z3Utils.exactOne(exactArray);
        exactExpr = z3Utils.imply(parent, exactExpr);
        BoolExpr conjoinExpr = z3Utils.conjoin(conjoinArray);

        Trio<Integer, BoolExpr, List<BoolExpr>> result = new Trio<>(len, z3Utils.conjoin(exactExpr, conjoinExpr), varList);
        return result;
    }
}
