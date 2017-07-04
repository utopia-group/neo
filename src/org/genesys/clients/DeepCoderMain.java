package org.genesys.clients;

import com.google.gson.Gson;
import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.interpreter.Interpreter;
import org.genesys.interpreter.L2Interpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.language.L2Grammar;
import org.genesys.models.Problem;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DefaultSynthesizer;
import org.genesys.synthesis.DummyChecker;
import org.genesys.synthesis.Synthesizer;
import org.genesys.type.InputType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;
import org.genesys.type.PairType;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by yufeng on 6/4/17.
 */
public class DeepCoderMain {

    public static void main(String[] args) throws FileNotFoundException {
        String json = "./problem/DeepCoder/prog00.json";
        if (args.length != 0) json = args[0];
        Gson gson = new Gson();
        Problem dcProblem = gson.fromJson(new FileReader(json), Problem.class);
        System.out.println("Run DeepCoder main..." + dcProblem);

        DeepCoderGrammar grammar = new DeepCoderGrammar(new PairType(new ListType(new IntType()),
                new ListType(new IntType())), new IntType());
        InputType in1 = new InputType(0, new ListType(new IntType()));
        InputType in2 = new InputType(1, new ListType(new IntType()));
        /* dynamically add input to grammar. */
        grammar.addInput(in1);
        grammar.addInput(in2);

        Checker checker = new DummyChecker();
        Interpreter interpreter = new DeepCoderInterpreter();
        Synthesizer synth = new DefaultSynthesizer(grammar, dcProblem, checker, interpreter);
        synth.synthesize();
    }
}
