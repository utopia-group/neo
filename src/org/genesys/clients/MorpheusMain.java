package org.genesys.clients;

import com.google.gson.Gson;
import org.genesys.decide.Decider;
import org.genesys.decide.FirstDecider;
import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.language.Grammar;
import org.genesys.language.MorpheusGrammar;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.models.Problem;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DeepCoderChecker;
import org.genesys.synthesis.DummyChecker;
import org.genesys.synthesis.NeoSynthesizer;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Yu on 7/6/17.
 */
public class MorpheusMain {

    public static void main(String[] args) throws FileNotFoundException {
        String json = "./problem/DeepCoder/prog5.json";
        String specLoc = "./specs/DeepCoder";
        if (args.length != 0) json = args[0];
        Gson gson = new Gson();
        Problem dcProblem = gson.fromJson(new FileReader(json), Problem.class);
        System.out.println("Run Morpheus main..." + dcProblem);

        Grammar grammar = new MorpheusGrammar(dcProblem);
        /* Load component specs. */
        Checker checker = new DummyChecker();
        Interpreter interpreter = new DeepCoderInterpreter();
        Decider decider = new FirstDecider();

        NeoSynthesizer synth = new NeoSynthesizer(grammar, dcProblem, checker, interpreter, specLoc, decider);
        synth.synthesize();
    }
}
