package org.genesys.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.genesys.language.Grammar;
import org.genesys.language.Production;
import org.genesys.models.Node;

public class DefaultProgramSampler<T> implements Sampler<Node> {
	private final Grammar<T> grammar;
	private final Random random;
	public DefaultProgramSampler(Grammar<T> grammar, Random random) {
		this.grammar = grammar;
		this.random = random;
	}
	public Node sample() {
		return this.sample(this.grammar.start());
	}
	private Node sample(T symbol) {
		List<Production<T>> productions = this.grammar.productionsFor(symbol);
		Production<T> production = productions.get(this.random.nextInt(productions.size()));
		List<Node> children = new ArrayList<Node>();
		for(T input : production.inputs) {
			children.add(this.sample(input));
		}
		return new Node(production.function, children);
	}
}
