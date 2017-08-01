package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;

import org.genesys.decide.Decider;
import org.genesys.ml.Utils.Counter;
import org.genesys.models.Node;
import org.genesys.models.Pair;

public class NGramDecider implements Decider {
	private final int nGramLength;
	private final Counter<Pair<String,String>> counts = new Counter<Pair<String,String>>();
	private final String NO_FUNCTION = "";
	
	public NGramDecider(List<Node> programs, int nGramLength) {
		this.nGramLength = nGramLength;
		for(Node program : programs) {
			for(Pair<String,List<String>> ancestor : DatasetGenerator.getFunctionData(program)) {
				this.counts.increment(new Pair<String,String>(this.getNGram(ancestor.t1), ancestor.t0));
			}
		}
	}
	
	private String getNGram(List<String> ancestors) {
		List<String> nGram = new ArrayList<String>();
		for(int i=0; i<this.nGramLength; i++) {
			String curFunction = ancestors.size() > i ? ancestors.get(ancestors.size() - i - 1) : NO_FUNCTION;
			nGram.add(curFunction);
		}
		return Utils.toString(nGram);
	}
	
	@Override
	public String decide(List<String> ancestors, List<String> functionChoices) {
		String curFunction = null;
		int curCount = 0;
		String nGram = getNGram(ancestors);
		for(String function : functionChoices) {
			int count = this.counts.getCount(new Pair<String,String>(nGram, function));
			if(curFunction == null || count > curCount) {
				curFunction = function;
				curCount = count;
			}
		}
		if(curFunction == null) {
			throw new RuntimeException("No valid choices provided!");
		}
		return curFunction;
	}
}
