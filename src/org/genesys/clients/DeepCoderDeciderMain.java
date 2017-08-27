package org.genesys.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.genesys.decide.Decider;
import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.DeepCoderInputSampler;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.ml.DeepCoderXFeaturizer;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.L2InputSamplerParameters;
import org.genesys.ml.L2XFeaturizerParameters;
import org.genesys.ml.NGramDecider;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class DeepCoderDeciderMain {
	public static void main(String[] args) {
		boolean isPython = true;
		if(isPython) {
			buildDeepCoderPythonDecider();
		} else {
			buildNGramDecider();
		}
	}
	
	public static void buildDeepCoderPythonDecider() {
		// parameters
		int maxDepth = 4;
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
		
		int minLength = 3;
		int maxLength = 5;
		int minValue = -10;
		int maxValue = 10;
        L2InputSamplerParameters inputSamplerParameters = new L2InputSamplerParameters(minLength, maxLength, maxValue, minValue);
		
		int nGramLength = 2;
		L2XFeaturizerParameters xFeaturizerParameters = new L2XFeaturizerParameters(inputSamplerParameters, nGramLength);
		
		// grammar
        DeepCoderGrammar grammar = new DeepCoderGrammar(new ListType(new IntType()), new IntType());
        
        // random
        Random random = new Random();
		
		// interpreter
        DeepCoderInterpreter interpreter = new DeepCoderInterpreter();
        
        // functions
        List<String> functions = new ArrayList<String>();
        for(String function : new TreeSet<String>(interpreter.executors.keySet())) {
        	functions.add(function);
        }
        
        // featurizers
        XFeaturizer<Object> xFeaturizer = new DeepCoderXFeaturizer(functions, xFeaturizerParameters);
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
        ancestors.add("last");
        ancestors.add("sort");
        List<String> functionChoices = new ArrayList<>();
        functionChoices.add("last");
        functionChoices.add("filter");
        functionChoices.add("sum");
        String nextChoice = decider.decide(ancestors, functionChoices);
        System.out.println("Previous: " + ancestors + " Next decision: " + nextChoice);
	}
	
	public static void buildNGramDecider() {
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
