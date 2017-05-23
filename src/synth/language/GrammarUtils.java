package synth.language;

import java.util.List;

import synth.utils.Utils.MultivalueMap;

public class GrammarUtils {
	public static interface Grammar<T> {
		public T start();
		public List<Production<T>> productionsFor(T symbol);
	}
	
	public static class SimpleGrammar<T> implements Grammar<T> {
		private final T start;
		private final MultivalueMap<T,Production<T>> productionsBySymbol = new MultivalueMap<T,Production<T>>();
		public SimpleGrammar(T start, MultivalueMap<T,Production<T>> productionsBySymbol) {
			this.start = start;
			for(T symbol : this.productionsBySymbol.keySet()) {
				for(Production<T> production : this.productionsBySymbol.get(symbol)) {
					this.productionsBySymbol.add(symbol, production);
				}
			}
		}
		@Override
		public T start() {
			return this.start;
		}
		public List<Production<T>> productionsFor(T symbol) {
			return this.productionsBySymbol.get(symbol);
		}
	}
	
	public static class Production<T> {
		public final String function;
		public final T[] inputs;
		@SafeVarargs
		public Production(String function, T ... inputs) {
			this.function = function;
			this.inputs = inputs;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.function);
			if(this.inputs.length > 0) {
				sb.append("(");
				for(T t : this.inputs) {
					sb.append(t.toString()).append(", ");
				}
				sb.delete(sb.length()-2, sb.length());
				sb.append(")");
			}
			return sb.toString();
		}
	}
	
	public static class Node {
		public final String function;
		public final Node[] children;
		@SafeVarargs
		public Node(String function, Node ... children) {
			this.function = function;
			this.children = children;
		}
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(this.children.length > 0) {
				sb.append("(");
			}
			sb.append(this.function).append(" ");
			for(Node child : this.children) {
				sb.append(child.toString()).append(" ");
			}
			sb.deleteCharAt(sb.length()-1);
			if(this.children.length > 0) {
				sb.append(")");
			}
			return sb.toString();
		}
	}
}
