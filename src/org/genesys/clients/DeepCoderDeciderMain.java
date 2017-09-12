package org.genesys.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.DeepCoderInputSampler;
import org.genesys.ml.DeepCoderInputSamplerParameters;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.ml.DeepCoderXFeaturizer;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class DeepCoderDeciderMain {
	public static void main(String[] args) {
		// parameters
		int maxDepth = 4;
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
        DeepCoderInputSamplerParameters inputSamplerParameters = DeepCoderPythonDecider.getDeepCoderParameters();
		
		// grammar
        DeepCoderGrammar grammar = new DeepCoderGrammar(new ListType(new IntType()), new IntType());
        
        // random
        Random random = new Random();
		
		// interpreter
        DeepCoderInterpreter interpreter = new DeepCoderInterpreter();
        
        // functions
        List<String> functions = DeepCoderPythonDecider.getDeepCoderFunctions();
        
        // featurizers
        XFeaturizer<Object> xFeaturizer = new DeepCoderXFeaturizer(inputSamplerParameters);
        YFeaturizer yFeaturizer = new YFeaturizer(functions);
        
        // sampler
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, programSamplerParameters, random);
        Sampler<Object> inputSampler = new DeepCoderInputSampler(grammar.inputType, inputSamplerParameters, random);
        
        // sample program and input
		Node program = programSampler.sample();
		System.out.println("SAMPLED PROGRAM: " + program);
		Object input = inputSampler.sample();
		System.out.println("SAMPLED INPUT: " + input);
		Object output = interpreter.execute(program, input).get();
		System.out.println("COMPUTED OUTPUT: " + output);
        
        // decider
        DeepCoderPythonDecider decider = new DeepCoderPythonDecider(xFeaturizer, yFeaturizer, input, output);
        
        // test decider
        List<String> ancestors = new ArrayList<>();
        ancestors.add("HEAD");
        ancestors.add("LAST");
        List<String> functionChoices = new ArrayList<>();
        functionChoices.add("LAST");
        functionChoices.add("ACCESS");
        functionChoices.add("SUM");
        String nextChoice = decider.decide(ancestors, functionChoices);
        System.out.println("Previous: " + ancestors + " Next decision: " + nextChoice);
	}
}
