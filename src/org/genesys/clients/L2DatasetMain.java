package org.genesys.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.genesys.interpreter.L2Interpreter;
import org.genesys.language.L2Grammar;
import org.genesys.ml.DataPoint;
import org.genesys.ml.DatasetGenerator;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultYFeaturizer;
import org.genesys.ml.L2InputSampler;
import org.genesys.ml.L2InputSamplerParameters;
import org.genesys.ml.L2XFeaturizer;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class L2DatasetMain {
	public static void main(String[] args) {
		int nGramLength = 2;
		int maxLength = 5;
		int minInt = -10;
		int maxInt = 10;
		
        L2Grammar grammar = new L2Grammar(new ListType(new IntType()), new IntType());
        L2Interpreter interpreter = new L2Interpreter();
        Random random = new Random();
        
        List<String> functions = new ArrayList<String>();
        for(String function : new TreeSet<String>(interpreter.executors.keySet())) {
        	functions.add(function);
        }
        
        XFeaturizer<Object> xFeaturizer = new L2XFeaturizer(functions, nGramLength);
        YFeaturizer yFeaturizer = new DefaultYFeaturizer(functions);
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, random);
        
        L2InputSamplerParameters parameters = new L2InputSamplerParameters(maxLength, maxInt, minInt);
        Sampler<Object> inputSampler = new L2InputSampler(grammar.inputType, parameters, random);
        
		List<DataPoint> data = DatasetGenerator.generateDataset(interpreter, xFeaturizer, yFeaturizer, programSampler, inputSampler);
		for(DataPoint datapoint : data) {
			System.out.println(datapoint);
		}
	}
}
