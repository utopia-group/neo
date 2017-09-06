package org.genesys.clients;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import krangl.DataFrame;
import krangl.SimpleDataFrameKt;
import org.genesys.decide.Decider;
import org.genesys.decide.FirstDecider;
import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.interpreter.Interpreter;
import org.genesys.interpreter.MorpheusInterpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.language.Grammar;
import org.genesys.language.MorpheusGrammar;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.models.Example;
import org.genesys.models.Problem;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DeepCoderChecker;
import org.genesys.synthesis.DummyChecker;
import org.genesys.synthesis.NeoSynthesizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yu on 9/3/17.
 */
public class MorpheusMain {

    public static void main(String[] args) throws FileNotFoundException {
        String json = "./problem/Morpheus/p4.json";
        String specLoc = "./specs/Morpheus";
        if (args.length != 0) json = args[0];
        Gson gson = new Gson();
        Problem problem = gson.fromJson(new FileReader(json), Problem.class);
        System.out.println("Run Morpheus main..." + problem);

        Problem tableProblem = new Problem();
        List<Example> tgtExamples = new ArrayList<>();
        for(Example org : problem.getExamples()) {
            Example tgt = new Example();
            List inputTgt = new ArrayList();
            for(Object o : org.getInput()) {
                //Get the input
                LinkedTreeMap out = (LinkedTreeMap)o;
                List<String> inHeader = (List) out.get("header");
                List<String> inContent = (List) out.get("content");
                String[] arrIn = inHeader.toArray(new String[inHeader.size()]);
                DataFrame inDf = SimpleDataFrameKt.dataFrameOf(arrIn).invoke(inContent.toArray());
                inputTgt.add(inDf);
            }
            tgt.setInput(inputTgt);
            //Get the output
            LinkedTreeMap out = (LinkedTreeMap)org.getOutput();
            List<String> outHeader = (List) out.get("header");
            System.out.println("header:" + outHeader);
            List<String> outContent = (List) out.get("content");
            System.out.println("content:" + outContent);
            String[] arr = outHeader.toArray(new String[outHeader.size()]);
            DataFrame outDf = SimpleDataFrameKt.dataFrameOf(arr).invoke(outContent.toArray());
            tgt.setOutput(outDf);
            tgtExamples.add(tgt);
        }
        tableProblem.setExamples(tgtExamples);

        Grammar grammar = new MorpheusGrammar(problem);
        /* Load component specs. */
        Checker checker = new DummyChecker();
        Interpreter interpreter = new MorpheusInterpreter();
        Decider decider = new FirstDecider();

        boolean useStat;
        NeoSynthesizer synth;
        if (args.length == 4) {
            useStat = Boolean.valueOf(args[3]);
            if(useStat)
                decider = new DeepCoderPythonDecider(problem, interpreter);

            int depth = Integer.valueOf(args[1]);
            boolean learning = Boolean.valueOf(args[2]);

            synth = new NeoSynthesizer(grammar, tableProblem, checker, interpreter, depth, specLoc, learning, decider);
        } else {
            synth = new NeoSynthesizer(grammar, tableProblem, checker, interpreter, specLoc, decider);
        }
        synth.synthesize();
    }
}
