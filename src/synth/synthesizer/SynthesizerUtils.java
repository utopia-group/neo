package synth.synthesizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import synth.language.GrammarUtils.Grammar;
import synth.language.GrammarUtils.Node;

public class SynthesizerUtils {
	public static interface Synthesizer {
		public <T,S> Node synthesize(Grammar<T> grammar, S specification, Checker<S> checker);
	}
	
	public static interface Checker<S> {
		public boolean check(S specification, Node node);
	}
	
	public static class CombinationChecker<S> implements Checker<S> {
		private final List<Checker<S>> checkers = new ArrayList<Checker<S>>();
		public CombinationChecker(Collection<Checker<S>> checkers) {
			this.checkers.addAll(checkers);
		}
		@Override
		public boolean check(S specification, Node node) {
			for(Checker<S> checker : this.checkers) {
				if(!checker.check(specification, node)) {
					return false;
				}
			}
			return true;
		}
	}
}
