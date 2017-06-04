package org.genesys.clients;

import org.genesys.language.L2Grammar;
import org.genesys.synthesis.Checker;
import org.genesys.synthesis.DefaultSynthesizer;
import org.genesys.synthesis.DummyChecker;
import org.genesys.synthesis.Synthesizer;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

/**
 * Created by yufeng on 6/4/17.
 */
public class L2Main {

    public static void main(String[] args) {
        System.out.println("Run L2 main...");

        L2Grammar l2Grammar = new L2Grammar(new ListType(new IntType()), new ListType(new IntType()));
        Checker checker = new DummyChecker();
        Synthesizer synth = new DefaultSynthesizer(l2Grammar, null, checker, null);
        synth.synthesize();
    }
}
