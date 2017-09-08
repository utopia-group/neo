package org.genesys.clients;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import org.genesys.interpreter.DeepCoderInterpreter;
import org.genesys.language.DeepCoderGrammar;
import org.genesys.ml.Datapoint;
import org.genesys.ml.DatasetGenerator;
import org.genesys.ml.DeepCoderInputSampler;
import org.genesys.ml.DeepCoderInputSamplerParameters;
import org.genesys.ml.DeepCoderPythonDecider;
import org.genesys.ml.DeepCoderXFeaturizer;
import org.genesys.ml.DefaultProgramSampler;
import org.genesys.ml.DefaultProgramSamplerParameters;
import org.genesys.ml.RawDatapoint;
import org.genesys.ml.Sampler;
import org.genesys.ml.XFeaturizer;
import org.genesys.ml.YFeaturizer;
import org.genesys.models.Node;
import org.genesys.models.Pair;
import org.genesys.type.AbstractType;
import org.genesys.type.IntType;
import org.genesys.type.ListType;

public class DeepCoderDatasetMain {
	public static void main(String[] args) {
		int maxDepth = 4;
		DefaultProgramSamplerParameters programSamplerParameters = new DefaultProgramSamplerParameters(maxDepth);
		
		int minLength = 5;
		int maxLength = 20;
		int minValue = -256;
		int maxValue = 255;
        DeepCoderInputSamplerParameters inputSamplerParameters = new DeepCoderInputSamplerParameters(minLength, maxLength, maxValue, minValue);
		
		int numIterations = 100000;
		
        DeepCoderGrammar grammar = new DeepCoderGrammar(new ListType(new IntType()), new IntType());
        DeepCoderInterpreter interpreter = new DeepCoderInterpreter();
        Random random = new Random();
        
        List<String> functions = DeepCoderPythonDecider.getDeepCoderFunctions();
        
        XFeaturizer<Object> xFeaturizer = new DeepCoderXFeaturizer(inputSamplerParameters);
        YFeaturizer yFeaturizer = new YFeaturizer(functions);
        Sampler<Node> programSampler = new DefaultProgramSampler<AbstractType>(grammar, programSamplerParameters, random);
        Sampler<Object> inputSampler = new DeepCoderInputSampler(grammar.inputType, inputSamplerParameters, random);
        
        List<RawDatapoint<Object>> rawDataset = DatasetGenerator.generateDataset(interpreter, programSampler, inputSampler, numIterations);
		Pair<List<Datapoint>,List<String>> dataset = DatasetGenerator.translateDataset(rawDataset, xFeaturizer, yFeaturizer);
		
		try {
			PrintWriter pw = new PrintWriter(new FileWriter("model/data/deep_coder.txt"));
			for(Datapoint datapoint : dataset.t0) {
				pw.println(datapoint);
			}
			pw.close();
			pw = new PrintWriter(new FileWriter("model/data/deep_coder_funcs.txt"));
			for(String function : dataset.t1) {
				pw.println(function);
			}
			pw.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
