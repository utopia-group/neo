package org.genesys.clients;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.genesys.interpreter.L2Interpreter;
import org.genesys.language.L2Grammar;
import org.genesys.ml.Datapoint;
import org.genesys.ml.DatasetGenerator;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.L2InputSampler;
import org.genesys.ml.L2InputSamplerParameters;
import org.genesys.ml.L2XFeaturizer;
import org.genesys.ml.L2XFeaturizerParameters;
import org.genesys.ml.RawDatapoint;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class L2DatasetMain {
	public static void main(String[] args) {
		int maxDepth = 4;
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
		
		int minLength = 3;
		int maxLength = 5;
		int minValue = -10;
		int maxValue = 10;
        L2InputSamplerParameters inputSamplerParameters = new L2InputSamplerParameters(minLength, maxLength, maxValue, minValue);
		
		int nGramLength = 2;
		L2XFeaturizerParameters xFeaturizerParameters = new L2XFeaturizerParameters(inputSamplerParameters, nGramLength);
		
		int numIterations = 100;
		
        L2Grammar grammar = new L2Grammar(new ListType(new IntType()), new ListType(new IntType()));
        L2Interpreter interpreter = new L2Interpreter();
        Random random = new Random();
        
        List<String> functions = new ArrayList<String>();
        for(String function : new TreeSet<String>(interpreter.executors.keySet())) {
        	functions.add(function);
        }
        
        XFeaturizer<Object> xFeaturizer = new L2XFeaturizer(functions, xFeaturizerParameters);
        YFeaturizer yFeaturizer = new YFeaturizer(functions);
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, programSamplerParameters, random);
        Sampler<Object> inputSampler = new L2InputSampler(grammar.inputType, inputSamplerParameters, random);
        
		List<RawDatapoint<Object>> rawDataset = DatasetGenerator.generateDataset(interpreter, programSampler, inputSampler, numIterations);
		Pair<List<Datapoint>,List<String>> dataset = DatasetGenerator.translateDataset(rawDataset, xFeaturizer, yFeaturizer);
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("model/data/l2.txt"));
			for(Datapoint datapoint : dataset.t0) {
				pw.println(datapoint);
			}
			pw.close();
			pw = new PrintWriter(new FileWriter("model/data/l2_funcs.txt"));
			for(String function : dataset.t1) {
				pw.println(function);
			}
			pw.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
