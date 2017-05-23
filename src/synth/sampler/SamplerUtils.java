package synth.sampler;

import java.util.List;
import java.util.Random;

import synth.language.GrammarUtils.Grammar;
import synth.language.GrammarUtils.Node;
import synth.language.GrammarUtils.Production;
import synth.utils.Utils.Maybe;

public class SamplerUtils {
	public static class RandomNodeSampler {
		private final Random random;
		public RandomNodeSampler(Random random) {
			this.random = random;
		}
		public <T> Node sample(Grammar<T> grammar, Maybe<Node> node, T symbol) {
			return sample(grammar, symbol);
		}
		private <T> Node sample(Grammar<T> grammar, T symbol) {
			List<Production<T>> productions = grammar.productionsFor(symbol);
			Production<T> production = productions.get(this.random.nextInt(productions.size()));
			Node[] children = new Node[production.inputs.length];
			for(int i=0; i<children.length; i++) {
				children[i] = this.sample(grammar, production.inputs[i]);
			}
			return new Node(production.function, children);
		}
	}
}
