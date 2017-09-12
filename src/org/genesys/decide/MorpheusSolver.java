package org.genesys.decide;

import com.microsoft.z3.BoolExpr;
import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.utils.SATUtils;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.Lbool;

import java.util.*;

/**
 * Created by ruben on 9/11/17.
 */
public class MorpheusSolver implements AbstractSolver<BoolExpr, Node> {

    private Decider decider_;

    private SATUtils satUtils_;

    private Grammar grammar_;

    /* Size of the program */
    private int maxLen_ = 4;

    /* Maximum number of children of a node */
    private int maxChildren_ = 0;

    /* Producions from inputs */
    private final List<Production> inputProductions_ = new ArrayList<>();
    private final List<List<Production>> lineProductions_ = new ArrayList<>();

    /* Maps types to productions */
    private Map<String, List<Production>> prodTypes_ = new HashMap<>();

    /* Maps production to symbols */
    private Map<String, Object> prodSymbols_ = new HashMap<>();

    /* Domain of productions */
    private final List<Production> domainProductions_ = new ArrayList<>();
    private final List<Production> domainHigher_ = new ArrayList<>();
    private final List<Production> domainFirst_ = new ArrayList<>();
    private final List<Production> domainOutput_ = new ArrayList<>();
    private final List<Production> domainInput_ = new ArrayList<>();

    private final HashMap<String,List<Integer>> higherGrouping_ = new HashMap<>();

    private List<Node> loc_ = new ArrayList<Node>();

    private boolean init_ = false;

    private int nodeId_ = 1;

    private int programs_ = 0;
    private int step_ = 1;

    //private int line_ = 0;
    private int currentLine_ = 0;

    private final Map<Pair<Integer, Production>, Integer> varNodes_ = new HashMap<>();
    private final Map<Pair<Integer, String>, Integer> nameNodes_ = new HashMap<>();

    private int nbVariables_ = 0;

    private List<Node> nodes_ = new ArrayList<>();

    private List<Pair<Node, Integer>> highTrail_ = new ArrayList<>();
    private final List<List<Pair<Node, Integer>>> trail_ = new ArrayList<>();
    private final List<Pair<Node,Pair<Integer,Integer>>> trailNeo_ = new ArrayList<>();
    private final List<Integer> trailSAT_ = new ArrayList<>();
    private List<Integer> currentSATLevel_ = new ArrayList<>();

    private final List<List<Integer>> backtrack_ = new ArrayList<>();

    private int level_ = 0;
    private int currentChild_ = 0;

    private boolean partial_ = false;

    public MorpheusSolver(Grammar g, Decider decider) {
        satUtils_ = SATUtils.getInstance();
        grammar_ = g;
        decider_ = decider;
        Object start = grammar_.start();
    }

    public MorpheusSolver(Grammar g, int depth, Decider decider) {
        satUtils_ = SATUtils.getInstance();
        maxLen_ = depth;
        grammar_ = g;
        decider_ = decider;
        Object start = grammar_.start();
        for (int i  = 0 ; i < maxLen_; i++){
            trail_.add(new ArrayList<>());
            backtrack_.add(new ArrayList<>());
            lineProductions_.add(new ArrayList<>());
        }
    }


    @Override
    public Node getModel(BoolExpr core, boolean block) {
        if (!init_) {
            init_ = true;
            loadGrammar();
            initDataStructures();
        } else {
            if (block) {
                boolean conflict = blockModel();
                if (conflict) {
                    System.out.println("programs=" + programs_);
                    return null;
                }
            }
            partial_ = false;
        }

        Node node = search();
        return node;
    }

    public Node getCoreModel(List<Pair<Integer, List<String>>> core, boolean block) {
        if (!init_) {
            init_ = true;
            loadGrammar();
            initDataStructures();
        } else {
            boolean conflict = blockModel();
            if (conflict) {
                System.out.println("programs=" + programs_);
                return null;
            }
            else {
//                boolean confl = learnCore(core);
//                if (confl){
//                    System.out.println("s UNSATISFIABLE - learning core");
//                    return null;
//                }
            }
            partial_ = false;
        }

        Node node = search();
        return node;
    }

    @Override
    public boolean isPartial(){
        return true;
    }

    private Node createNode(List<Production> root, List<Production> children) {

        Node node = new Node("", new ArrayList<>(), root);
        node.id = nodeId_++;
        nodes_.add(node);
        for (int i = 0; i < maxChildren_; i++) {
            Node child = new Node("", new ArrayList<>(), children);
            child.id = nodeId_++;
            nodes_.add(child);
            node.addChild(child);
        }
        return node;
    }


    private void initDataStructures() {

        // Each line has its own subtree
        for (int i = 0; i < maxLen_; i++){
            Node node = null;

            List<Production> domain = new ArrayList<>();
            domain.addAll(domainFirst_);
            for (int j = i-1; j >= 0; j--)
                domain.addAll(lineProductions_.get(j));

            if (i == 0) node = createNode(domainInput_,domain);
            else if (i == maxLen_-1) node = createNode(domainOutput_,domain);
            else node = createNode(domainHigher_,domain);

            createVariables(node);

            loc_.add(node);
            highTrail_.add(new Pair<Node, Integer>(node,highTrail_.size()+1));
        }

        // Create empty SAT solver
        satUtils_.createSolver();
        satUtils_.createVars(nbVariables_);

        buildSATFormula();

        // Decision level for Neo and SAT solver
        level_ = 0;
        currentLine_ = 0;
        currentSATLevel_.add(0);

    }

    private void buildSATFormula() {


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

        // At most one variable is assigned at each node
        for (Node node : nodes_) {
            VecInt clause = new VecInt();
            for (Production p : node.domain){
                int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));
                clause.push(productionVar);
            }
            satUtils_.addAMK(clause,1);
        }

        Iterator it = higherGrouping_.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            VecInt amk = new VecInt();
            for (int i = 0 ;i < ((List)pair.getValue()).size(); i++){
                amk.push(((List<Integer>)pair.getValue()).get(i));

                if (i != ((List)pair.getValue()).size()-1){
                    // There are no two consecutive higher order function calls
                    VecInt clause = new VecInt(new int[]{-((List<Integer>)pair.getValue()).get(i),-((List<Integer>)pair.getValue()).get(i+1)});
                    satUtils_.addClause(clause);
                }
            }
            // At most 2 occurrences of each higher order component in the sketch
            satUtils_.addAMK(amk, 2);
            it.remove();
        }

        // At least one input must be used in the first line of code
        VecInt clause_input = new VecInt();
        for (Node node : highTrail_.get(0).t0.children){


            for (Production p : inputProductions_){
                int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));
                clause_input.push(productionVar);
            }
        }
        satUtils_.addClause(clause_input);


        // The intermediate results cannot be used before they are created
        // ---> This is encoded in the domain


        // An intermediate result is used exactly once (do not allow let binding)
        List<VecInt> letbind = new ArrayList<>();
        for (int i = 0; i < maxLen_; i++)
            letbind.add(new VecInt());

        for (int i = 0 ; i < maxLen_; i++){
            for (int j = i-1; j >= 0; j--){
                for (Production p : lineProductions_.get(j)){
                    for (Node node : highTrail_.get(i).t0.children) {
                        int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));
                        letbind.get(j).push(productionVar);
                    }
                }
            }
        }

        for (int i = 0; i < maxLen_; i++){
            if (!letbind.get(i).isEmpty())
                satUtils_.addEO(letbind.get(i),1);
        }

        // Every input is used exactly once (we can change this if needed)
        List<List<Integer>> inputs_used = new ArrayList<List<Integer>>();
        for (int i = 0 ; i < inputProductions_.size(); i++)
            inputs_used.add(new ArrayList<>());

        for (int i = 0; i < maxLen_; i++) {
            for (Production p : inputProductions_){
                for (Node node : highTrail_.get(i).t0.children) {
                    assert (varNodes_.containsKey(new Pair<Integer, Production>(node.id, p)));
                    int var = varNodes_.get(new Pair<Integer, Production>(node.id, p));
                    String[] parts = p.function.split("input");
                    assert (parts.length == 2);
                    inputs_used.get(Integer.valueOf(parts[1])).add(var);
                }
            }
        }

        for (int i = 0; i < inputs_used.size(); i++){
            VecInt clause = new VecInt();
            for (int j = 0; j < inputs_used.get(i).size(); j++){
                clause.push(inputs_used.get(i).get(j));
            }
            //satUtils_.addClause(clause);
            satUtils_.addEO(clause, 1);
        }

        // If an intermediate result has type T then it cannot have type T'
        // ---> is a consequence of the previous statement when we do not allow let binding


        // If an intermediate result if of type T then the root has to be a production rule of that type
        for (int i = 0; i < maxLen_-1; i++) {

            if (i == 0) {
                for (Production p : domainInput_) {
                    Node node = highTrail_.get(i).t0;
                    int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));

                    for (int j = maxLen_-1; j > i; j--){
                        for (Node n : highTrail_.get(j).t0.children) {
                            for (Production l : lineProductions_.get(i)) {
                                assert (varNodes_.containsKey(new Pair<Integer, Production>(n.id, l)));
                                int lineVar = varNodes_.get(new Pair<Integer, Production>(n.id, l));
                                if (!p.source.equals(l.source)) {
                                    VecInt clause = new VecInt(new int[]{-productionVar, -lineVar});
                                    satUtils_.addClause(clause);
                                }
                            }
                        }
                    }
                }
            } else {
                for (Production p : domainHigher_) {
                    Node node = highTrail_.get(i).t0;
                    int productionVar = varNodes_.get(new Pair<Integer, Production>(node.id, p));

                    for (int j = maxLen_-1; j > i; j--){
                        for (Node n : highTrail_.get(j).t0.children) {
                            for (Production l : lineProductions_.get(i)) {
                                assert (varNodes_.containsKey(new Pair<Integer, Production>(n.id, l)));
                                int lineVar = varNodes_.get(new Pair<Integer, Production>(n.id, l));
                                if (!p.source.equals(l.source)) {
                                    VecInt clause = new VecInt(new int[]{-productionVar, -lineVar});
                                    satUtils_.addClause(clause);
                                }
                            }
                        }
                    }
                }
            }
        }

        Constr conflict = satUtils_.propagate();
        assert (conflict == null);
    }

    private <T> void loadGrammar() {
        List<Production<T>> prods = grammar_.getProductions();
        prods.addAll(grammar_.getInputProductions());
        //prods.addAll(grammar_.getLineProductions(maxLen_));

        inputProductions_.addAll((List<Production<T>>) grammar_.getInputProductions());

        for (Production<T> prod : (List<Production<T>>) grammar_.getLineProductions(maxLen_)){
            for (int i = 0 ; i < maxLen_; i++){
                if (prod.function.startsWith("line" + Integer.toString(i))){
                    lineProductions_.get(i).add(prod);
                }
            }
        }

        prods.addAll(grammar_.getLineProductions(maxLen_));

        for (Production<T> prod : prods) {

            if (!prodTypes_.containsKey(prod.source.toString())) {
                prodTypes_.put(prod.source.toString(), new ArrayList<Production>());
            }

            prodTypes_.get(prod.source.toString()).add(prod);

            prodSymbols_.put(prod.function, prod.source);

            //prodName_.put(prod.function, prod);

            if (prod.inputs.length > maxChildren_)
                maxChildren_ = prod.inputs.length;

            if (prod.function.startsWith("line"))
                continue;

            domainProductions_.add(prod);

            if (prod.higher){
                domainHigher_.add(prod);

                if (grammar_.getOutputType().toString().compareTo(prod.source.toString()) == 0)
                    domainOutput_.add(prod);

                for (Production<T> p : (List<Production<T>>) grammar_.getInputProductions()){
                    if (prod.source.toString().compareTo(p.source.toString())==0) {
                        domainInput_.add(prod);
                        break;
                    }
                }
            }
            else {
                domainFirst_.add(prod);
            }

        }
    }

    private <T> void createVariables(Node node) {

        for (Production p : node.domain) {
            Pair<Integer, Production> pair = new Pair<Integer, Production>(node.id, p);
            Pair<Integer, String> pair2 = new Pair<Integer, String>(node.id, p.function);
            varNodes_.put(pair, ++nbVariables_);
            nameNodes_.put(pair2,nbVariables_);
            if (p.higher) {
                if (!higherGrouping_.containsKey(p.function)) {
                    higherGrouping_.put(p.function, new ArrayList<>());
                    higherGrouping_.get(p.function).add(nbVariables_);
                } else higherGrouping_.get(p.function).add(nbVariables_);
            }

        }

        for (int i = 0; i < node.children.size(); i++)
            createVariables(node.children.get(i));
    }

    public String nextDecision(List<String> domain) {


        String decision = decider_.decide(new ArrayList<>(), domain);
        assert (!decision.equals(""));
        return decision;
    }

    private Node decideInputs() {

        Production decisionNeo = null;
        int decisionSAT = -1;

        Node node = trail_.get(currentLine_).get(currentChild_).t0;
        Map<String, Pair<Production, Integer>> decideMap = new HashMap<>();
        List<String> decideDomain = new ArrayList<>();
        for (Production p : node.domain) {
            if (p.function.startsWith("input") || p.function.startsWith("line")) {
                int var = varNodes_.get(new Pair<Integer, Production>(node.id, p));

                if (satUtils_.getSolver().truthValue(var) == Lbool.UNDEFINED ||
                        satUtils_.getSolver().truthValue(var) == Lbool.TRUE) {
                    decideMap.put(p.function, new Pair<Production, Integer>(p, var));
                    decideDomain.add(p.function);
                }
            }
        }

        if (!decideDomain.isEmpty()) {
            String decision = nextDecision(decideDomain);
            Pair<Production, Integer> p = decideMap.get(decision);
            decisionNeo = p.t0;
            decisionSAT = p.t1;
        }

        if (decisionNeo == null)
            return null;
        else {
            node.function = decisionNeo.function;
            node.decision = decisionNeo;
            node.level = level_;

//            System.out.println("NEO decision Inputs = " + decisionNeo.function + " @" + level_ + " node ID = " + node.id + " SAT decision= " + decisionSAT + " assume= " + satUtils_.posLit(decisionSAT));

            Pair<Integer,Integer> p = new Pair<Integer,Integer>(currentLine_,currentChild_);
            Pair<Node, Pair<Integer,Integer>> p2 = new Pair<Node, Pair<Integer,Integer>>(node, p);

            trailNeo_.add(p2);
            trailSAT_.add(decisionSAT);
            assert (satUtils_.getSolver().truthValue(decisionSAT) != Lbool.FALSE);
            if (satUtils_.getSolver().truthValue(decisionSAT) == Lbool.UNDEFINED)
                satUtils_.getSolver().assume(satUtils_.posLit(decisionSAT));

            currentSATLevel_.add(satUtils_.getSolver().decisionLevel());

            for (int i = 0; i < decisionNeo.inputs.length; i++) {
                trail_.get(level_).add(new Pair<Node, Integer>(node.children.get(i), level_));
            }
            level_++;

            return node;
        }
    }


    public Node decideFirst(){

        Production decisionNeo = null;
        int decisionSAT = -1;

        Node node = trail_.get(currentLine_).get(currentChild_).t0;
        Map<String, Pair<Production, Integer>> decideMap = new HashMap<>();
        List<String> decideDomain = new ArrayList<>();
        for (Production p : node.domain) {
            int var = varNodes_.get(new Pair<Integer, Production>(node.id, p));

            if (satUtils_.getSolver().truthValue(var) == Lbool.UNDEFINED ||
                    satUtils_.getSolver().truthValue(var) == Lbool.TRUE) {
                decideMap.put(p.function, new Pair<Production, Integer>(p, var));
                decideDomain.add(p.function);
            }
        }

        if (!decideDomain.isEmpty()) {
            String decision = nextDecision(decideDomain);
            Pair<Production, Integer> p = decideMap.get(decision);
            decisionNeo = p.t0;
            decisionSAT = p.t1;
        }

        if (decisionNeo == null)
            return null;
        else {
            node.function = decisionNeo.function;
            node.decision = decisionNeo;
            node.level = level_;

//            System.out.println("NEO decision = " + decisionNeo.function + " @" + level_ + " node ID = " + node.id + " SAT decision= " + decisionSAT + " assume= " + satUtils_.posLit(decisionSAT));

            Pair<Integer,Integer> p = new Pair<Integer,Integer>(currentLine_,currentChild_);
            Pair<Node, Pair<Integer,Integer>> p2 = new Pair<Node, Pair<Integer,Integer>>(node, p);

            trailNeo_.add(p2);
            trailSAT_.add(decisionSAT);
            assert (satUtils_.getSolver().truthValue(decisionSAT) != Lbool.FALSE);
            if (satUtils_.getSolver().truthValue(decisionSAT) == Lbool.UNDEFINED)
                satUtils_.getSolver().assume(satUtils_.posLit(decisionSAT));

            currentSATLevel_.add(satUtils_.getSolver().decisionLevel());

            for (int i = 0; i < decisionNeo.inputs.length; i++) {
                trail_.get(level_).add(new Pair<Node, Integer>(node.children.get(i), level_));
            }
            level_++;

            return node;
        }




    }

    public Node decideHigh(){

        assert (level_ < highTrail_.size());
        Production decisionNeo = null;
        int decisionSAT = -1;

        Node node = highTrail_.get(level_).t0;
        Map<String, Pair<Production, Integer>> decideMap = new HashMap<>();
        List<String> decideDomain = new ArrayList<>();
        for (Production p : node.domain) {
            int var = varNodes_.get(new Pair<Integer, Production>(node.id, p));

            if (satUtils_.getSolver().truthValue(var) == Lbool.UNDEFINED ||
                    satUtils_.getSolver().truthValue(var) == Lbool.TRUE) {
                decideMap.put(p.function, new Pair<Production, Integer>(p, var));
                decideDomain.add(p.function);
            }
        }

        if (!decideDomain.isEmpty()) {
            String decision = nextDecision(decideDomain);
            Pair<Production, Integer> p = decideMap.get(decision);
            decisionNeo = p.t0;
            decisionSAT = p.t1;
        }

        if (decisionNeo == null)
            return null;
        else {
            node.function = decisionNeo.function;
            node.decision = decisionNeo;
            node.level = ++level_;

//            System.out.println("NEO decision = " + decisionNeo.function + " @" +level_ + " node ID = " + node.id + " SAT decision= " + decisionSAT + " assume= " + satUtils_.posLit(decisionSAT));


            Pair<Integer,Integer> p = new Pair<Integer,Integer>(currentLine_,currentChild_);
            Pair<Node, Pair<Integer,Integer>> p2 = new Pair<Node, Pair<Integer,Integer>>(node, p);

            trailNeo_.add(p2);
            trailSAT_.add(decisionSAT);
            assert (satUtils_.getSolver().truthValue(decisionSAT) != Lbool.FALSE);
            if (satUtils_.getSolver().truthValue(decisionSAT) == Lbool.UNDEFINED)
                satUtils_.getSolver().assume(satUtils_.posLit(decisionSAT));

            currentSATLevel_.add(satUtils_.getSolver().decisionLevel());

            for (int i = 0; i < decisionNeo.inputs.length; i++) {
                trail_.get(level_-1).add(new Pair<Node, Integer>(node.children.get(i), level_));
            }

            return node;
        }
    }

    private boolean blockModel() {

        //System.out.println("trail_.size = " + trailNeo_.size());
        boolean unsat = false;
//        while (backtrack(currentLevel_ - 1)) {
//            if (currentLevel_ == 0) {
//                System.out.println("s UNSATISFIABLE : backtracking block model");
//                unsat = true;
//                break;
//            }
//        }
        //unsat = backtrack(0, true);

//        currentLine_ = 0;
//        currentChild_ = 0;
//        level_ = 0;
//        currentSATLevel_.clear();
//        currentSATLevel_.add(0);
//        trailNeo_.clear();
//        for (int i = 0; i < trail_.size(); i++){
//            trail_.get(i).clear();
//        }
//        satUtils_.getSolver().cancelUntil(currentSATLevel_.get(0));
//        unsat = satUtils_.blockTrail(trailSAT_);
//        System.out.println("trailSAT= " + trailSAT_);
//        trailSAT_.clear();


        unsat = backtrackStep2(0, true);
        if (unsat)
            System.out.println("s UNSATISFIABLE : backtracking block model");

        return unsat;
    }

    private boolean backtrackStep1(int lvl, boolean block) {

        // There is a disparity between the level in Neo and the level in the SAT solvers
        // Several decisions in Neo may be in the same internal level in the SAT solver
        // When backtracking, we need to take into consideration the internals of the SAT solver
        int backtrack_lvl = lvl;
        while (currentSATLevel_.get(level_) == currentSATLevel_.get(backtrack_lvl) || backtrack_lvl > 0) {
            backtrack_lvl--;
        }

        assert (trailNeo_.size() > 0 && trailSAT_.size() > 0);
        int size = trailNeo_.size();

        if (backtrack_lvl < highTrail_.size()) {
            for (int i = backtrack_lvl; i < highTrail_.size(); i++)
                trail_.get(i).clear();
        }

        satUtils_.getSolver().cancelUntil(currentSATLevel_.get(backtrack_lvl));

        boolean conflict = false;
        if (block) conflict = satUtils_.blockTrail(trailSAT_);

        for (int i = size; i > backtrack_lvl; i--) {
            // undo
            trailNeo_.get(trailNeo_.size() - 1).t0.function = "";
            trailNeo_.get(trailNeo_.size() - 1).t0.decision = null;
            trailNeo_.get(trailNeo_.size() - 1).t0.level = -1;

            trailNeo_.remove(trailNeo_.size() - 1);
            trailSAT_.remove(trailSAT_.size() - 1);
        }
        level_ = backtrack_lvl;
        currentSATLevel_.subList(backtrack_lvl+1,currentSATLevel_.size()).clear();
        assert (currentSATLevel_.size() == level_ + 1);

        return conflict;
    }

    private boolean backtrackStep2(int lvl, boolean block) {

        // There is a disparity between the level in Neo and the level in the SAT solvers
        // Several decisions in Neo may be in the same internal level in the SAT solver
        // When backtracking, we need to take into consideration the internals of the SAT solver
        int backtrack_lvl = lvl;
        while (currentSATLevel_.get(level_) == currentSATLevel_.get(backtrack_lvl) || backtrack_lvl > 0) {
            backtrack_lvl--;
        }

        assert (trailNeo_.size() > 0 && trailSAT_.size() > 0);
        int size = trailNeo_.size();

        if (backtrack_lvl < highTrail_.size()) {
            for (int i = backtrack_lvl; i < highTrail_.size(); i++)
                trail_.get(i).clear();
        }

        satUtils_.getSolver().cancelUntil(currentSATLevel_.get(backtrack_lvl));

        boolean conflict = false;
        if (block) conflict = satUtils_.blockTrail(trailSAT_);

        for (int i = size; i > backtrack_lvl; i--) {
            // undo
            trailNeo_.get(trailNeo_.size() - 1).t0.function = "";
            trailNeo_.get(trailNeo_.size() - 1).t0.decision = null;
            trailNeo_.get(trailNeo_.size() - 1).t0.level = -1;

            currentLine_ = trailNeo_.get(trailNeo_.size() - 1).t1.t0;
            currentChild_ = trailNeo_.get(trailNeo_.size() - 1).t1.t1;

            trailNeo_.remove(trailNeo_.size() - 1);
            trailSAT_.remove(trailSAT_.size() - 1);
        }
        level_ = backtrack_lvl;
        currentSATLevel_.subList(backtrack_lvl+1,currentSATLevel_.size()).clear();
        assert (currentSATLevel_.size() == level_ + 1);

        return conflict;
    }

    private Node translate() {

        Object startNode = grammar_.start();
        Node root = new Node();
        root.setSymbol(startNode);
        root.function = "root";
        root.id=0;
        root.setConcrete(true);


        List<Node> ast = new ArrayList<>();
        for (int i = 0; i < highTrail_.size(); i++){
            Node node = highTrail_.get(i).t0;
            Node ast_node = new Node();

            assert(node.function.compareTo("")!=0);

            ast_node.id = node.id;
            ast_node.function = node.function;
            ast_node.setSymbol(prodSymbols_.get(ast_node.function));
            for (Node c: node.children){
                if (!c.function.equals("")) {
                    if (c.function.startsWith("line")){
                        String[] parts = c.function.split("line");
                        // Assumes only 1 digit
                        int index = Character.getNumericValue(parts[1].charAt(0));
                        ast_node.addChild(ast.get(index));
                    } else {
                        Node ch = new Node();
                        ch.function = c.function;
                        ch.id = c.id;
                        ch.setSymbol(prodSymbols_.get(ch.function));
                        ast_node.addChild(ch);
                    }
                }
            }
            ast.add(ast_node);
        }

        assert(!ast.isEmpty());
        root.addChild(ast.get(ast.size()-1));

        return root;
    }


    private boolean backtrackStep3(int lvl, boolean block) {

        // There is a disparity between the level in Neo and the level in the SAT solvers
        // Several decisions in Neo may be in the same internal level in the SAT solver
        // When backtracking, we need to take into consideration the internals of the SAT solver
        int backtrack_lvl = lvl;
        while (currentSATLevel_.get(level_) == currentSATLevel_.get(backtrack_lvl) || backtrack_lvl > 0) {
            backtrack_lvl--;
        }

        assert (trailNeo_.size() > 0 && trailSAT_.size() > 0);
        int size = trailNeo_.size();

        if (backtrack_lvl < highTrail_.size()) {
            for (int i = backtrack_lvl; i < highTrail_.size(); i++)
                trail_.get(i).clear();
        }

        satUtils_.getSolver().cancelUntil(currentSATLevel_.get(backtrack_lvl));

        boolean conflict = false;
        if (block) conflict = satUtils_.blockTrail(trailSAT_);

        for (int i = size; i > backtrack_lvl; i--) {
            // undo
            trailNeo_.get(trailNeo_.size() - 1).t0.function = "";
            trailNeo_.get(trailNeo_.size() - 1).t0.decision = null;
            trailNeo_.get(trailNeo_.size() - 1).t0.level = -1;

            trailNeo_.remove(trailNeo_.size() - 1);
            trailSAT_.remove(trailSAT_.size() - 1);
        }
        level_ = backtrack_lvl;
        currentSATLevel_.subList(backtrack_lvl+1,currentSATLevel_.size()).clear();
        assert (currentSATLevel_.size() == level_ + 1);

        return conflict;
    }

    private void printProgram(Node node){
        String program = node.function;
        program += " ( ";
        for (Node n : node.children){
            if (!n.function.equals("")) {
                program += n.function + " ";
            }
        }
        program += " ) ";
        System.out.println("Line= " + program);
    }

    public Node search() {
        Node result = null;

        boolean unsat = false;
        step_ = 1;

        while (!unsat) {

            if (step_ == 1) {

                // STEP 1. Decide all higher-order components
                while (level_ < highTrail_.size()) {
                    //System.out.println("level_ " + level_);
                    if (partial_) break;

                    if (unsat) break;

                    Constr conflict = satUtils_.getSolver().propagate();
                    if (conflict != null) {
                        int backjumpLevel = satUtils_.analyzeSATConflict(conflict);
                        if (backjumpLevel == -1) {
                            unsat = true;
                            break;
                        } else backtrackStep1(backjumpLevel, false);
                    } else {
                        // No conflict
                        Node decision = decideHigh();
                        if (decision == null) {
                            if (level_ == 0) {
                                unsat = true;
                                break;
                            }

                            while (backtrackStep1(level_ - 1, true)) {
                                if (level_ == 0) {
                                    unsat = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (unsat) {
                    System.out.println("s NO SOLUTION");
                    System.out.println("programs=" + programs_);
                    break;
                }

                step_ = 2;
            }

            if (step_ == 2) {

                // STEP 2. Decide on all inputs/lines
                currentLine_ = 0;
                currentChild_ = 0;
                boolean repeat_step2 = false;

                while (currentLine_ < trail_.size()) {
                    boolean children_assigned = false;
                    while (currentChild_ < trail_.get(currentLine_).size()) {

                        Constr conflict = satUtils_.getSolver().propagate();
                        if (conflict != null) {
                            int backjumpLevel = satUtils_.analyzeSATConflict(conflict);

                            if (backjumpLevel < highTrail_.size()) {
                                backtrackStep2(backjumpLevel, false);
                                step_ = 1;
                                break;
                            } else {
                                backtrackStep2(backjumpLevel, false);
                                if (level_ < highTrail_.size()){
                                    step_ = 1;
                                    break;
                                } else {
                                    System.out.println("level_ = " + level_);
                                    repeat_step2 = true;
                                    break;
                                }
                            }

                        } else {
                            Node decision = decideInputs();
                            if (decision != null)
                                children_assigned = true;
                            currentChild_++;
                        }
                    }

                    if (!children_assigned && step_ == 2 && !repeat_step2){
                        // conflict -- backtrack needed
                        assert(false);

                        System.out.println("level_ = " + level_);

                        while (backtrackStep2(level_ - 1, true)) {
                            System.out.println("level_ = " + level_);
                            if (level_ == 0) {
                                unsat = true;
                                break;
                            }
                        }
                        System.out.println("level = " + level_);

                        System.out.println("children not assigned!");
                        assert(false);
                    }

                    if (step_ == 1 || repeat_step2){
                        currentChild_ = 0;
                        currentLine_ = 0;
                        break;
                    }
                    currentChild_ = 0;
                    currentLine_++;
                }

                if (!repeat_step2 && step_ == 2) {

                    // Check that we are in a consistent state
                    Constr conflict = satUtils_.getSolver().propagate();
                    if (conflict != null) {
                        int backjumpLevel = satUtils_.analyzeSATConflict(conflict);
                        assert(false);
                    }

                    assert (conflict == null);
                    step_ = 3;
                }
            }
//
//            // Partial program that contains only the higher-order components
//            // Decide first-order bottom-up
//            System.out.println("currentLine = " + currentLine_);
//            while (currentLine_ < trail_.size()) {
//                while (currentChild_ < trail_.get(currentLine_).size()) {
//
//                    Constr conflict = satUtils_.getSolver().propagate();
//                    if (conflict != null) {
//                        System.out.println("currentChild = " + currentChild_ + " trail_ " + trail_.get(currentLine_).size());
//
//                        System.out.println("****Conflict in the SAT");
//                        System.out.println("Current level = " + level_);
//                        int backjumpLevel = satUtils_.analyzeSATConflict(conflict);
//                        System.out.println("backjumplevel= " + backjumpLevel);
//
//                        if (backjumpLevel == -1) {
//                            System.out.println("s UNSATISFIABLE : SAT Conflict");
//                            unsat = true;
//                            break;
//                        }
//
//                        backtrackFirst(0, false);
//                        backtracking = true;
//                        break;
////
////                        else backtrack(backjumpLevel, false);
////
////
////                        System.out.println("currentChild = " + currentChild_ + " trail_ " + trail_.get(currentLine_).size());
////
////                        assert (backjumpLevel >= highTrail_.size());
////                        System.out.println("currentLine = " + currentLine_ + " currentChild = " + currentChild_);
////
////                        // we need to figure out where to backtrack...
////                        // each line has a currentChild which corresponds to a decision level
////                        // we also need to figure out which line to backtrack...
////                        // everytime we backtrack we need to put back in the trail
////
////                        System.out.println("currentLine = " + currentLine_ + " currentChild = " + currentChild_);
////
////
////                        //if (backjumpLevel < highTrail_.size()) {
//////                            backtrack(0, false);
//////                            backtracking = true;
//////                            break;
//                        //}
//
//                    } else {
//                        System.out.println("no conflict");
//                        // No conflict
//                        Node decision = decideFirst();
//                        currentChild_++;
//                        if (decision == null) {
//                            System.out.println("decision is null!");
//                            if (level_ == 0) {
//                                System.out.println("s UNSATISFIABLE : lvl = 0");
//                                unsat = true;
//                                break;
//                            }
//
//                            while (backtrackFirst(level_ - 1, true)) {
//                                if (level_ == 0) {
//                                    System.out.println("s UNSATISFIABLE : backtracking");
//                                    unsat = true;
//                                    break;
//                                }
//                            }
//
//                            assert (level_ < highTrail_.size());
//                            backtracking = true;
//                            break;
//
//
////                            backtrack(0, false);
////                            backtracking = true;
////                            break;
//
//                        }
//                    }
//                    System.out.println("currentChild = " + currentChild_ + " trail_ " + trail_.get(currentLine_).size());
//                }
//
//                if (backtracking){
//                    currentLine_ = 0;
//                    currentChild_ = 0;
//                    break;
//                } else {
//
//                    System.out.println("printing programs");
//                    for (Pair<Node, Integer> n : highTrail_) {
//                        printProgram(n.t0);
//                    }
//
//                    currentLine_++;
//                    currentChild_ = 0;
//                }
//            }


            if (step_ == 3){

                if (unsat) {
                    System.out.println("programs=" + programs_);
                    break;
                }

                step_ = 1;
                programs_++;
                Node ast = translate();
                return ast;
//
//
//                if (blockModel()) {
//                    System.out.println("programs=" + programs);
//                    break;
//                } else {
//                    programs++;
//                }
            }


        }

        return result;
    }


}
