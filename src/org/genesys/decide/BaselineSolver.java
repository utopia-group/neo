package org.genesys.decide;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.models.Trio;
import org.genesys.utils.LibUtils;
import org.genesys.utils.Z3Utils;

import java.util.*;

/**
 * Created by yufeng on 5/31/17.
 */
public class BaselineSolver implements AbstractSolver<BoolExpr, Node> {

    private Z3Utils z3Utils;

    private Grammar grammar_;

    private int maxLen_ = 3;

    private final Map<String, Production> ctrlVarMap = new HashMap<>();

    private final Map<String, Node> ctrlVarAstMap = new HashMap<>();

    private Node astRoot;

    private Model model_;

    public BaselineSolver(Grammar g) {
        z3Utils = Z3Utils.getInstance();
        grammar_ = g;
        Object start = grammar_.start();
        System.out.println("start symbol:" + start);
        astRoot = new Node("root");
        astRoot.setCtrlVar("true");
        ctrlVarAstMap.put("true", astRoot);
        Trio<Integer, BoolExpr, List<BoolExpr>> formula = generate(grammar_, start, z3Utils.trueExpr(), maxLen_);
        System.out.println("Big formula: " + formula.t1);
        z3Utils.init(formula.t1);
    }

    @Override
    public Node getModel(BoolExpr core) {
        if (model_ != null) {
            boolean hasNext = z3Utils.blockSolution();
            if (!hasNext) return null;
        }
        model_ = z3Utils.getModel();
//        System.out.println("Current model:" + model_);
        Node ast = translate(model_);
        return ast;
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
//                System.out.println("Parent: " + var + " Child------------" + subResult.t2);

//                System.out.println("prods.size= " + prods.size() + " prod= " + prod.toString());

                for (BoolExpr subVar : subResult.t2) {
                    /* if child happens, that implies parent also happens. */
                    conjoinList.add(z3Utils.imply(subVar, var));
//                    if (prods.size() == 1)
//                        conjoinList.add(z3Utils.imply(var, subVar));

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

    /**
     * Translate Z3 model into a concrete AST node.
     *
     * @param m: Z3 model.
     * @return AST node
     */
    private Node translate(Model m) {
        if (m == null) return null;

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

//        System.out.println("program: " + astRoot.traverseModel(models));
        Node sol = extractAst(models);
        return sol;
    }

    private Node extractAst(Set<String> models) {
        Queue<Pair<Node, Node>> worklist = new LinkedList<>();
        Node rootCopy = new Node(astRoot);
        Pair<Node, Node> rootPair = new Pair<>(astRoot, rootCopy);
        worklist.add(rootPair);
        while (!worklist.isEmpty()) {
            Pair<Node, Node> workerPair = worklist.remove();
            Node org = workerPair.t0;
            Node copy = workerPair.t1;

            for (Node child : org.children) {
                if (models.contains(child.getCtrlVar())) {
                    Node childCopy = new Node(child);
                    worklist.add(new Pair<>(child, childCopy));
                    copy.addChild(childCopy);
                }
            }
        }

        return rootCopy;
    }

}
