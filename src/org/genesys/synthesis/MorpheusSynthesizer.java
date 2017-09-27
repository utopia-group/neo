package org.genesys.synthesis;

import com.google.gson.Gson;
import com.microsoft.z3.BoolExpr;
import krangl.DataFrame;
import krangl.Extensions;
import krangl.ReshapeKt;
import krangl.SimpleDataFrame;
import org.genesys.decide.AbstractSolver;
import org.genesys.decide.Decider;
import org.genesys.decide.MorpheusSolver;
import org.genesys.decide.NeoSolver;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.Grammar;
import org.genesys.models.*;
import org.genesys.type.Maybe;
import org.genesys.utils.LibUtils;
import org.genesys.utils.Z3Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by utcs on 9/11/17.
 */
public class MorpheusSynthesizer implements Synthesizer {

    private AbstractSolver<BoolExpr, Node> solver_;

    private boolean silent_ = true;

    private boolean learning_ = true;

    private Checker checker_;

    private Interpreter interpreter_;

    private Problem problem_;

    private double totalSearch = 0.0;

    private double totalTest = 0.0;

    private double totalDeduction = 0.0;

    private HashMap<Integer, Component> components_ = new HashMap<>();

    private Gson gson = new Gson();


    public MorpheusSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter, String specLoc, Decider decider) {
        solver_ = new MorpheusSolver(grammar, decider);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;

        File[] files = new File(specLoc).listFiles();
        for (File file : files) {
            assert file.isFile() : file;
            String json = file.getAbsolutePath();
            Component comp = null;
            try {
                comp = gson.fromJson(new FileReader(json), Component.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            components_.put(comp.getId(), comp);
        }
    }

    public MorpheusSynthesizer(Grammar grammar, Problem problem, Checker checker, Interpreter interpreter, int depth, String specLoc, boolean learning, Decider decider){
        learning_ = learning;
        solver_ = new MorpheusSolver(grammar, depth, decider);
        checker_ = checker;
        interpreter_ = interpreter;
        problem_ = problem;

        File[] files = new File(specLoc).listFiles();
        for (File file : files) {
            assert file.isFile() : file;
            String json = file.getAbsolutePath();
            Component comp = null;
            try {
                comp = gson.fromJson(new FileReader(json), Component.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            components_.put(comp.getId(), comp);
        }
    }

    @Override
    public Node synthesize() {

        /* retrieve an AST from the solver */
        Node ast = solver_.getModel(null, false);
        int total = 0;
        int prune_concrete = 0;
        int prune_partial = 0;
        int concrete = 0;
        int partial = 0;

        while (ast != null) {
            /* do deduction */
            total++;
            if (solver_.isPartial()) partial++;
            else concrete++;

//            System.out.println("Checking Program: " + ast);
            long start = LibUtils.tick();
//            boolean isSatisfiable = true;
            boolean isSatisfiable = checker_.check(problem_, ast);
            long end = LibUtils.tick();
            totalDeduction += LibUtils.computeTime(start, end);

            if (!isSatisfiable) {
                if (learning_) {
                    Z3Utils z3 = Z3Utils.getInstance();
                    List<Pair<Integer, List<Integer>>> conflicts = z3.getConflicts();
                    List<Pair<Integer, List<String>>> convert = new ArrayList<>();
                    for (Pair<Integer, List<Integer>> p : conflicts) {
                        Pair<Integer, List<String>> new_p = new Pair<>(p.t0, new ArrayList<>());
                        for (Integer l : p.t1) {
                            assert components_.containsKey(l);
                            new_p.t1.add(components_.get(l).getName());
                        }
                        convert.add(new_p);
                    }
                    long start2 = LibUtils.tick();
                    solver_.cacheAST(ast.toString(),true);
                    ast = solver_.getCoreModel(convert, true);
                    long end2 = LibUtils.tick();
                    totalSearch += LibUtils.computeTime(start2, end2);
                } else {
                    long start2 = LibUtils.tick();
                    solver_.cacheAST(ast.toString(),true);
                    ast = solver_.getModel(null, true);
                    long end2 = LibUtils.tick();
                    totalSearch += LibUtils.computeTime(start2, end2);
                }
                if (solver_.isPartial()) prune_partial++;
                else prune_concrete++;
                continue;
            }


            if (solver_.isPartial()){
                if(!silent_) System.out.println("Partial Program: " + ast);
                long start2 = LibUtils.tick();
                solver_.cacheAST(ast.toString(),false);
                ast = solver_.getModel(null, false);
                long end2 = LibUtils.tick();
                totalSearch += LibUtils.computeTime(start2, end2);
                continue;
            } else {

                /* check input-output using the interpreter */
                long start2 = LibUtils.tick();
                boolean isCorrect = verify(ast);
                long end2 = LibUtils.tick();
                totalTest += LibUtils.computeTime(start2,end2);

                if (isCorrect) {
                    System.out.println("Synthesized PROGRAM: " + ast);
                    break;
                } else {
                    long start3 = LibUtils.tick();
                    solver_.cacheAST(ast.toString(),true);
                    ast = solver_.getModel(null, true);
                    long end3 = LibUtils.tick();
                    totalSearch += LibUtils.computeTime(start3, end3);
                }
            }
        }
        System.out.println("Concrete programs=: " + concrete);
        System.out.println("Partial programs=: " + partial);
        System.out.println("Search time=:" + (totalSearch));
        System.out.println("Deduction time=:" + (totalDeduction));
        System.out.println("Test time=:" + (totalTest));
        System.out.println("Total=:" + total);
        System.out.println("Prune partial=:" + prune_partial + " %=:" + prune_partial*100.0/partial);
        System.out.println("Prune concrete=:" + prune_concrete + " %=:" + prune_concrete*100.0/concrete);

        return ast;
    }

    /* Verify the program using I-O examples. */
    private boolean verify(Node program) {
        long start = LibUtils.tick();
        boolean passed = true;
        if (!silent_)  System.out.println("Program: " + program);
        for (Example example : problem_.getExamples()) {
            //FIXME:lets assume we only have at most two input tables for now.
            Object input = LibUtils.fixGsonBug(example.getInput());
            // Always one output table
            Object output = LibUtils.fixGsonBug(example.getOutput());
            try {
                Maybe<Object> tgt = interpreter_.execute(program, input);
                if (tgt == null){
                    passed = false;
                    break;
                }

//                System.out.println("result target:\n" + ((SimpleDataFrame)tgt.get()).getCols());
//                Extensions.print((SimpleDataFrame)tgt.get());
//                System.out.println("expected target:\n" + ((SimpleDataFrame)output).getCols());
//                Extensions.print((SimpleDataFrame)output);

                if (output instanceof DataFrame) {
                    boolean flag = ReshapeKt.hasSameContents((DataFrame) tgt.get(), (SimpleDataFrame) output);
                    if (!flag) {
                        passed = false;
                        break;
                    }
                } else {
                    if (!tgt.has() || !tgt.get().equals(output)) {
                        passed = false;
                        break;
                    }
                }
            } catch (Exception e) {
                if (!silent_) System.out.println("Exception= " + e);
                passed = false;
                //e.printStackTrace();
                //assert false;
                break;
            }
        }
        long end = LibUtils.tick();
        totalTest += LibUtils.computeTime(start, end);
        return passed;
    }

}
