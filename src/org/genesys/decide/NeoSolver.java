package org.genesys.decide;

import com.microsoft.z3.BoolExpr;
import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.models.Trio;
import org.genesys.utils.SATUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.*;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.TimeoutException;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by ruben on 7/6/17.
 */
public class NeoSolver implements AbstractSolver<BoolExpr, Node> {

    private SATUtils satUtils_;

    private Grammar grammar_;

    private int maxLen_ = 4;

    private int nodeId_ = 1;

    /* Maps types to productions */
    private Map<String, List<Production>> prodTypes_ = new HashMap<>();

    /* Domain of productions */
    private final List<Production> domainProductions_ = new ArrayList<>();
    private final List<Production> domainLeafProductions_ = new ArrayList<>();
    private final List<Production> domainRootProductions_ = new ArrayList<>();

    /* Maps variables to node id */
    private final Map<Pair<Integer, Production>, Integer> varNodes_ = new HashMap<>();

    /* Nodes */
    private final List<Node> leafNodes_ = new ArrayList<>();
    private final List<Node> nodes_ = new ArrayList<>();
    private final Map<Integer, Node> idNodes_ = new HashMap<Integer, Node>();
    private Node root_ = null;

    /* Producions from inputs */
    private final List<Production> inputProductions_ = new ArrayList<>();

    /* Trail */
//    List<Integer> enqueueSAT_ = new ArrayList<>();
//    List<Node> enqueueNeo_ = new ArrayList<>();

    List<Node> trailNeo_ = new ArrayList<>();
    List<Integer> trailSAT_ = new ArrayList<>();

    private int maxChildren_ = 0;

    private int nbProductions_ = 0;

    private int nbVariables_ = 0;

    private int currentLevel_ = 0;

    public NeoSolver(Grammar g) {
        satUtils_ = SATUtils.getInstance();
        grammar_ = g;
        Object start = grammar_.start();
    }

    public NeoSolver(Grammar g, int depth) {
        satUtils_ = SATUtils.getInstance();
        maxLen_ = depth;
        grammar_ = g;
        Object start = grammar_.start();
    }

    @Override
    public Node getModel(BoolExpr core) {
        return null;
    }

    public Node createNode(List<Production> productions, boolean children) {
        Node node = new Node("", new ArrayList<>(), productions);
        if (children) {
            for (int i = 0; i < maxChildren_; i++) {
                Node child = new Node("", new ArrayList<>(), productions);
                node.addChild(child);
            }
        }
        return node;
    }

    /* Assumes that the root was already created */
    public void createTree(Node node, int depth) {
        nodes_.add(node);
        node.id = nodeId_++;

        assert (node.children.size() == maxChildren_);
        for (int i = 0; i < node.children.size(); i++) {
            if (depth == maxLen_) {
                // leaf node
                node.children.get(i).setDomain(domainLeafProductions_);
                leafNodes_.add(node);
                assert (((Node) node.children.get(i)).children.isEmpty());
            } else {
                node.children.get(i).setDomain(domainProductions_);
                assert (((Node) node.children.get(i)).children.isEmpty());
                for (int j = 0; j < maxChildren_; j++)
                    ((Node) node.children.get(i)).addChild(createNode(domainProductions_, false));
            }
        }

        if (depth < maxLen_) {
            for (int i = 0; i < node.children.size(); i++)
                createTree(((Node) node.children.get(i)), depth + 1);
        } else {
            for (int i = 0; i < node.children.size(); i++) {
                node.children.get(i).id = nodeId_++;
                assert (!nodes_.contains(nodes_.add(node.children.get(i))));
                nodes_.add(node.children.get(i));
            }
        }
    }

    public <T> void createVariables(Node node) {

        // Create variables for node
        for (Production p : node.domain) {
            Pair<Integer, Production> pair = new Pair<Integer, Production>(node.id, p);
            varNodes_.put(pair, ++nbVariables_);
        }

        for (int i = 0; i < node.children.size(); i++)
            createVariables(node.children.get(i));

//        for (int i = 0; i < root.children.size(); i++) {
//            for (Production p : root.domain) {
//                Pair<Integer, Production> key = new Pair<Integer, Production>(root.children.get(i).id, p);
//                varNodes_.put(key, ++nbVariables_);
//            }
//            createVariables((Node) root.children.get(i));
//        }

    }

    public boolean findModel(Deque<Node> pending) {

        boolean ok = true;


        return ok;
    }

    public void printTree(Node root) {
        List<Node> bfs = new ArrayList<>();
        bfs.add(root);
        while (!bfs.isEmpty()) {
            Node node = bfs.remove(bfs.size() - 1);
            assert (!node.domain.isEmpty());
            for (Production p : node.domain) {
                System.out.println("Node " + node.id + " | Production= " + p.function);
            }
            for (int i = 0; i < node.children.size(); i++)
                bfs.add(node.children.get(i));
        }
    }

    public boolean backtrack(int lvl, Deque<Pair<Node, Integer>> trail) {

        System.out.println("Backtracking to lvl = " + lvl);
        assert (trailNeo_.size() > 0 && trailSAT_.size() > 0);

        int size = trailNeo_.size();

        for (Iterator<Pair<Node, Integer>> iter = trail.iterator(); iter.hasNext(); ) {
            Pair<Node, Integer> p = iter.next();
            if (p.t1 >= lvl)
                iter.remove();
        }
        trail.add(new Pair<Node, Integer>(trailNeo_.get(trailNeo_.size() - 1), lvl));

        satUtils_.getSolver().cancelUntil(lvl);
        boolean conflict = satUtils_.blockTrail(trailSAT_);

        for (int i = size; i > lvl; i--) {
            trailNeo_.remove(trailNeo_.size() - 1);
            trailSAT_.remove(trailSAT_.size() - 1);
        }
        currentLevel_ = lvl;

        return conflict;
    }

    private boolean inputsUsed() {

        boolean used = true;
        for (Production p : inputProductions_) {
            ArrayList<Integer> clause = new ArrayList<>();
            for (Node node : nodes_) {
                Pair<Integer, Production> pair = new Pair<Integer, Production>(node.id, p);
                if (varNodes_.containsKey(pair)) {
                    clause.add(varNodes_.get(pair));
                }
            }
            boolean ok = false;
            for (Integer var : clause) {
                if (satUtils_.getSolver().truthValue(var) == Lbool.TRUE) {
                    ok = true;
                    break;
                }
            }
            used = used && ok;
        }

        return used;
    }

    public void search(Node node) {

        // Nodes that still need to be processed
        Deque<Pair<Node, Integer>> trail = new LinkedList<>();
        currentLevel_ = 0;

        trail.add(new Pair<Node, Integer>(node, currentLevel_));

        // Create empty SAT solver
        satUtils_.createSolver();
        satUtils_.createVars(nbVariables_);

        buildGrammarSATFormula();
        System.out.println("#Constraints = " + satUtils_.getSolver().nConstraints());
        System.out.println("#Variables = " + satUtils_.getSolver().nVars());
        System.out.println("trail= " + satUtils_.getSolver().trail.toString());

        boolean unsat = false;
        while (!unsat) {
            while (!trail.isEmpty()) {

                if (unsat) break;
                Constr conflict = propagate();

                if (conflict != null) {
                    System.out.println("SAT Conflict");
                    int backjumpLevel = satUtils_.analyzeSATConflict(conflict);
                    backtrack(backjumpLevel, trail);
                } else {
                    // No conflict
                    Node decision = decide(trail);
                    if (decision == null) {
                        System.out.println("Conflict in the decision!");
                        if (currentLevel_ == 0){
                            System.out.println("s UNSATISFIABLE : lvl = 0");
                            unsat = true;
                            break;
                        }

                        while (backtrack(currentLevel_ - 1, trail)) {
                            if (currentLevel_ == 0) {
                                System.out.println("s UNSATISFIABLE : backtracking");
                                unsat = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (unsat) break;

            if (inputsUsed()) {
                System.out.println("s SATISFIABLE");
                break;
            } else {
                // Conflict
                System.out.println("INPUTS NOT USED");
                while (backtrack(currentLevel_ - 1, trail)) {
                    if (currentLevel_ == 0) {
                        System.out.println("s UNSATISFIABLE : backtracking inputs");
                        unsat = true;
                        break;
                    }
                }
            }
        }

//                // Propagate the grammar
//                boolean confGrammar = propagationGrammar(node, pending);
//                if (confGrammar) {
//                    // Backtrack due to conflict on the Grammar
//
//                } else {
//                    // Propagate the SAT formula
//                    boolean confSAT = propagationSAT();
//                    if (confSAT) {
//                        // Backtrack due to conflict on the SAT formula
//                    }
//                }


//            if (unsat) {
//                System.out.println("s UNSATISFIABLE");
//                break;
//            }
//
//            if (!satUtils_.allVariablesAssigned()) {
//                if (satUtils_.blockTrail(trailSAT_)) {
//                    System.out.println("s UNSATISFIABLE");
//                    break;
//                } else {
//                    // backtrack
//                    System.out.println("not all variables are assigned!");
//                    assert (false);
////                    trailSAT_.pop();
////                    pending.addFirst(trail_.getLast().t0);
////                    trail_.remove(trail_.size() - 1);
////                    lvl--;
//                }
//            } else {
//                System.out.println("s SATISFIABLE");
//                break;
//            }//       }

    }

    public String decideML(List<String> current) {
        // Use ML techniques to find the best decision
        return "";
    }

    public Node decide(Deque<Pair<Node, Integer>> trail) {

        assert (!trail.isEmpty());
        Node node = decideNode(trail);

        Production decisionNeo = null;
        int decisionSAT = -1;

        for (Production p : node.domain) {
            int var = varNodes_.get(new Pair<Integer, Production>(node.id, p));
            if (satUtils_.getSolver().truthValue(var) == Lbool.UNDEFINED ||
                    satUtils_.getSolver().truthValue(var) == Lbool.TRUE) {
                decisionNeo = p;
                decisionSAT = var;
                break;
            }
        }

        if (decisionNeo == null)
            return null;
        else {

            node.function = decisionNeo.function;
            node.decision = decisionNeo;
            node.level = ++currentLevel_;
            System.out.println("NEO decision = " + decisionNeo.function + " @" + currentLevel_ + " node ID = " + node.id + " SAT decision= " + decisionSAT + " assume= " + satUtils_.posLit(decisionSAT));

            trailNeo_.add(node);
            trailSAT_.add(decisionSAT);
            assert (satUtils_.getSolver().truthValue(decisionSAT) != Lbool.FALSE);
            if (satUtils_.getSolver().truthValue(decisionSAT) == Lbool.UNDEFINED)
                satUtils_.getSolver().assume(satUtils_.posLit(decisionSAT));

            for (int i = 0; i < decisionNeo.inputs.length; i++) {
                trail.add(new Pair<Node, Integer>(node.children.get(i), currentLevel_));
            }

            return node;
        }
    }

    private Node decideNode(Deque<Pair<Node, Integer>> pending) {
        return pending.pollFirst().t0;
    }

    /* Propagation grammar goals:
     *  a) restrict the types of each children
     *  b) enqueue the corresponding SAT values
     */
    private boolean propagationGrammar(Node node, Deque<Node> pending) {

//        assert (node.decision != null);
//        boolean confl = false;
//
//        ArrayList<Integer> positive = new ArrayList<>();
//        ArrayList<Integer> negative = new ArrayList<>();
//
//        System.out.println("propagating= " + node.function + " children= " + node.children.size() + " inputs= " + node.decision.inputs.length);
//
//        for (int i = 0; i < node.decision.inputs.length; i++) {
//
//            List<Production> positiveProduction = new ArrayList<>();
//            for (Production p : prodTypes_.get(node.decision.inputs[i].toString())) {
//                System.out.println("production= " + p.toString());
//                if (node.children.get(i).domain.contains(p)) {
//                    positiveProduction.add(p);
//                }
//            }
//
//            int domainSize = 0;
//            for (Production p : node.children.get(i).domain) {
//                if (positiveProduction.contains(p)) {
//                    node.children.get(i).activeDomain.put(p, true);
//                    assert (varNodes_.containsKey(new Pair<Integer, Production>(node.children.get(i).id, p)));
//                    enqueueSAT_.add(varNodes_.get(new Pair<Integer, Production>(node.children.get(i).id, p)));
//                    domainSize++;
//                } else {
//                    node.children.get(i).activeDomain.put(p, false);
//                    assert (varNodes_.containsKey(new Pair<Integer, Production>(node.children.get(i).id, p)));
//                    enqueueSAT_.add(-varNodes_.get(new Pair<Integer, Production>(node.children.get(i).id, p)));
//                }
//            }
//            pending.add(node.children.get(i));
//
//            if (domainSize == 0 && !node.children.get(i).children.isEmpty()) {
//                confl = true;
//            }
//        }
//
//        // If this node contains less than k children then the remaining will be empty
//        for (int i = node.decision.inputs.length; i < maxChildren_; i++) {
//            for (Production p : node.children.get(i).domain) {
//                if (varNodes_.containsKey(new Pair<Integer, Production>(node.children.get(i).id, p))) {
//                    node.children.get(i).activeDomain.put(p, false);
//                    enqueueSAT_.add(-varNodes_.get(new Pair<Integer, Production>(node.children.get(i).id, p)));
//                }
//            }
//        }
//
//        return confl;
        return true;
    }

    private boolean propagationSAT() {

        return true;

//        System.out.println("variables = " + SATUtils.getInstance().getSolver().nVars());
//        System.out.println("constraints = " + SATUtils.getInstance().getSolver().nConstraints());
//
//        boolean conflict = false;
//
//        for (int i = 0; i < enqueueSAT_.size(); i++) {
//            int literal = 0;
//            if (enqueueSAT_.get(i) < 0) literal = satUtils_.negLit(-enqueueSAT_.get(i));
//            else literal = satUtils_.posLit(enqueueSAT_.get(i));
//            assert (literal > 1);
//            assert (Math.abs(enqueueSAT_.get(i)) < satUtils_.getSolver().nVars());
//            System.out.println("Assuming literal = " + literal + " variable= " + enqueueSAT_.get(i));
//
//            if (SATUtils.getInstance().getSolver().truthValue(enqueueSAT_.get(i)) == Lbool.UNDEFINED) {
//                conflict = !SATUtils.getInstance().getSolver().assume(literal);
//
//                if (!conflict) {
//                    Constr confl = SATUtils.getInstance().getSolver().propagate();
//                    if (confl != null) {
//                        conflict = true;
//                    }
//                }
//
//            } else {
//                if ((SATUtils.getInstance().getSolver().truthValue(enqueueSAT_.get(i)) == Lbool.TRUE &&
//                        enqueueSAT_.get(i) < 0) || (SATUtils.getInstance().getSolver().truthValue(enqueueSAT_.get(i)) == Lbool.FALSE &&
//                        enqueueSAT_.get(i) > 0)) {
//                    conflict = true;
//                }
//            }
//        }
//        enqueueSAT_.clear();
//        System.out.println("conflict = " + conflict);
//
//        return conflict;
    }

    private Constr propagate() {

        Constr satConflict = satUtils_.getSolver().propagate();
        return satConflict;
    }

    public void buildGrammarSATFormula() {

//        // AMO production at each node is used
//        for (Node node : nodes_) {
//            VecInt clause = new VecInt();
//            for (Production p : node.domain) {
//                Pair<Integer, Production> pair = new Pair<Integer, Production>(node.id, p);
//                assert (varNodes_.containsKey(pair));
//                clause.push(varNodes_.get(pair));
//            }
//            satUtils_.addAMO(clause);
//        }

        // If a production is used in a parent node then this implies restrictions on the children
        for (Node node : nodes_) {
            for (Production p : node.domain) {

                int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));

                for (int i = 0; i < p.inputs.length; i++) {
                    ArrayList<Production> occurs = new ArrayList<>();
                    VecInt clause = new VecInt();
                    clause.push(-productionVar);
                    for (Production pc : prodTypes_.get(p.inputs[i].toString())) {
                        if (node.children.get(i).domain.contains(pc)) {
                            Pair<Integer, Production> pair = new Pair<Integer, Production>(node.children.get(i).id, pc);
                            assert (varNodes_.containsKey(pair));
                            // Parent restricts the domain of the child (positively)
                            clause.push(varNodes_.get(pair));
                            occurs.add(pc);
                        }
                    }
                    if (clause.size() > 1)
                        satUtils_.addClause(clause);

                    for (Production pc : node.children.get(i).domain) {
                        if (!occurs.contains(pc)) {
                            VecInt lits = new VecInt(new int[]{-productionVar, -varNodes_.get(new Pair<Integer, Production>(node.children.get(i).id, pc))});
                            // Parent restricts the domain of child (negatively)
                            satUtils_.addClause(lits);
                        }
                    }
                }

                // If this node contains less than k children then the remaining will be empty
                if (!node.children.isEmpty()) {
                    for (int i = p.inputs.length; i < maxChildren_; i++) {
                        for (Production pc : node.children.get(i).domain) {
                            VecInt lits = new VecInt(new int[]{-productionVar, -varNodes_.get(new Pair<Integer, Production>(node.children.get(i).id, pc))});
                            satUtils_.addClause(lits);
                        }
                    }
                }
            }
        }

        // Inputs must be used in some node
//        for (Production p : inputProductions_) {
//            VecInt clause = new VecInt();
//            for (Node node : nodes_) {
//                Pair<Integer, Production> pair = new Pair<Integer, Production>(node.id, p);
//                if (varNodes_.containsKey(pair)) {
//                    clause.push(varNodes_.get(pair));
//                }
//            }
//            satUtils_.addClause(clause);
//        }

        Constr conflict = satUtils_.propagate();
        assert (conflict == null);
    }

    public <T> void loadGrammar() {
        List<Production<T>> prods = grammar_.getProductions();
        prods.addAll(grammar_.getInputProductions());

        inputProductions_.addAll((List<Production<T>>) grammar_.getInputProductions());

        for (Production<T> prod : prods) {
            nbProductions_++;

            if (!prodTypes_.containsKey(prod.source.toString())) {
                prodTypes_.put(prod.source.toString(), new ArrayList<Production>());
            }

            prodTypes_.get(prod.source.toString()).add(prod);

            if (prod.inputs.length > maxChildren_)
                maxChildren_ = prod.inputs.length;

            domainProductions_.add(prod);
            if (prod.inputs.length == 0)
                domainLeafProductions_.add(prod);

            if (grammar_.getOutputType().toString().compareTo(prod.source.toString()) == 0)
                domainRootProductions_.add(prod);
        }

        // build the k-tree
        root_ = createNode(domainRootProductions_, true);
        createTree(root_, 2);

        // root variable
//        for (int i = 0; i < root_.domain.size(); i++) {
//            varNodes_.put(new Pair<Integer, Production>(root_.id, root_.domain.get(i)), ++nbVariables_);
//        }
        createVariables(root_);
        printTree(root_);

        search(root_);

    }

}
