package org.genesys.clients;

import java.util.ArrayList;
import java.util.Arrays;
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
        XFeaturizer<Object> xFeaturizer = new DeepCoderXFeaturizer(inputSamplerParameters, functions);
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
		
		input = Arrays.asList(new List[]{Arrays.asList(new Integer[]{-17, -3, 4, 11, 0, -5, -9, 13, 6, 6, -8, 11})});
		output = Arrays.asList(new Integer[]{-12, -20, -32, -36, -68});
        
        // decider
        DeepCoderPythonDecider decider = new DeepCoderPythonDecider(xFeaturizer, yFeaturizer, input, output);
        
        // test decider
        List<String> ancestors = new ArrayList<String>();
        for(String function : functions) {
        	double probability = decider.getProbability(ancestors, function);
        	System.out.println(function + ": " + probability);
        	ancestors.add(function);
        }
	}
}
