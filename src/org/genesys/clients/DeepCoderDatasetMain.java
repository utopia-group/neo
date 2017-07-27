package org.genesys.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.DataPoint;
import org.genesys.ml.DatasetGenerator;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.DefaultYFeaturizer;
import org.genesys.ml.L2InputSampler;
import org.genesys.ml.L2InputSamplerParameters;
import org.genesys.ml.L2XFeaturizer;
import org.genesys.ml.L2XFeaturizerParameters;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class DeepCoderDatasetMain {
	public static void main(String[] args) {
		int maxDepth = 20;
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
		
		int maxLength = 5;
		int minValue = -10;
		int maxValue = 10;
        L2InputSamplerParameters inputSamplerParameters = new L2InputSamplerParameters(maxLength, maxValue, minValue);
		
		int nGramLength = 2;
		L2XFeaturizerParameters xFeaturizerParameters = new L2XFeaturizerParameters(inputSamplerParameters, nGramLength);
		
        DeepCoderGrammar grammar = new DeepCoderGrammar(new ListType(new IntType()), new IntType());
        DeepCoderInterpreter interpreter = new DeepCoderInterpreter();
        Random random = new Random();
        
        List<String> functions = new ArrayList<String>();
        for(String function : new TreeSet<String>(interpreter.executors.keySet())) {
        	functions.add(function);
        }
        
        XFeaturizer<Object> xFeaturizer = new L2XFeaturizer(functions, xFeaturizerParameters);
        YFeaturizer yFeaturizer = new DefaultYFeaturizer(functions);
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, programSamplerParameters, random);
        Sampler<Object> inputSampler = new L2InputSampler(grammar.inputType, inputSamplerParameters, random);
        
		List<DataPoint> data = DatasetGenerator.generateDataset(interpreter, xFeaturizer, yFeaturizer, programSampler, inputSampler);
		for(DataPoint datapoint : data) {
			System.out.println(datapoint);
		}
	}
}
