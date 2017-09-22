package org.genesys.clients;

import com.google.gson.Gson;
import org.genesys.language.ToyGrammar;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DefaultSynthesizer;
import org.genesys.synthesis.DummyChecker;
import org.genesys.synthesis.Synthesizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Created by yufeng on 5/26/17.
 */
public class DummyMain2 {

    public static void main(String[] args) throws FileNotFoundException {
        String str = "a_b_c";
        String str2 = "a|b|c";
        System.out.println(Arrays.asList(str.split("\\.|_")));
        System.out.println(Arrays.asList(str2.split("\\.|_|\\|")));
    }
}
