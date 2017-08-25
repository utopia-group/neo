package org.genesys.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.genesys.decide.Decider;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.NGramDecider;
import org.genesys.ml.Sampler;
import org.genesys.models.Node;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class DeepCoderNGramMain {
	public static void main(String[] args) {
		// parameters
		int numSamples = 100;
		int maxDepth = 20;
		int nGramLength = 2;
		
		// setup
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
        DeepCoderGrammar grammar = new DeepCoderGrammar(new ListType(new IntType()), new IntType());
        Random random = new Random();
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, programSamplerParameters, random);
        
        // sample programs
        List<Node> programs = new ArrayList<Node>();
        for(int i=0; i<numSamples; i++) {
            Node sample = programSampler.sample();
            System.out.println("sample:" + sample);
        	programs.add(sample);
        }
        
        // build n-gram statistics
        Decider decider = new NGramDecider(programs, nGramLength);
        List<String> ancestors = new ArrayList<>();
        ancestors.add("last");
        ancestors.add("sort");
        List<String> functionChoices = new ArrayList<>();
        functionChoices.add("last");
        functionChoices.add("filter");
        functionChoices.add("sum");
        String nextChoice = decider.decide(ancestors, functionChoices);
        System.out.println("Previous: " + ancestors + " Next decision: " + nextChoice);
	}
}