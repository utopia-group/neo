package org.genesys.clients;

import com.google.gson.Gson;
import org.genesys.language.*;
import org.genesys.synthesis.DefaultSynthesizer;
import org.genesys.synthesis.Synthesizer;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by yufeng on 5/26/17.
 */
public class DummyMain {

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();
        ToyGrammar toyGrammar = gson.fromJson(new FileReader("./grammar/Toy.json"), ToyGrammar.class);
        System.out.println("baseline synthesizer...");
        toyGrammar.init();
        Synthesizer synth = new DefaultSynthesizer(toyGrammar);
        synth.synthesize(toyGrammar, null, null);
        synth.nextSolution();
    }
}
