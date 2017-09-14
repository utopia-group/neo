package org.genesys.clients;

import com.google.gson.Gson;
import org.genesys.decide.Decider;
import org.genesys.decide.FirstDecider;
import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.interpreter.Interpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.models.Problem;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DeepCoderChecker;
import org.genesys.synthesis.MorpheusSynthesizer;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by utcs on 9/11/17.
 */
public class DeepCoderMainMorpheus {

    public static void main(String[] args) throws FileNotFoundException {
        boolean useStat;
        String specLoc = "./specs/DeepCoder";
        String json = "./problem/DeepCoder/prog5.json";
        if (args.length != 0) json = args[0];
        Gson gson = new Gson();
        Problem dcProblem = gson.fromJson(new FileReader(json), Problem.class);
        System.out.println("Run DeepCoder main..." + dcProblem);

        DeepCoderGrammar grammar = new DeepCoderGrammar(dcProblem);
        /* Load component specs. */
        Checker checker = new DeepCoderChecker(specLoc);
        //Checker checker = new DummyChecker();
        Interpreter interpreter = new DeepCoderInterpreter();
        Decider decider = new FirstDecider();

        MorpheusSynthesizer synth;
        if (args.length == 4) {
            useStat = Boolean.valueOf(args[3]);
            if(useStat)
                decider = new DeepCoderPythonDecider(dcProblem);

            int depth = Integer.valueOf(args[1]);
            boolean learning = Boolean.valueOf(args[2]);

            synth = new MorpheusSynthesizer(grammar, dcProblem, checker, interpreter, depth, specLoc, learning, decider);
        } else {
            synth = new MorpheusSynthesizer(grammar, dcProblem, checker, interpreter, specLoc, decider);
        }
        synth.synthesize();
    }
}
