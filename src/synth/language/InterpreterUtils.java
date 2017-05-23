package synth.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synth.language.GrammarUtils.Node;
import synth.utils.Utils.Maybe;

public class InterpreterUtils {
	public static class Interpreter {
		private final Map<String,Executor> executors = new HashMap<String,Executor>();
		public Interpreter(Map<String,Executor> executors) {
			this.executors.putAll(executors);
		}
		public Maybe<Object> execute(Node node, Object input) {
			List<Object> objects = new ArrayList<Object>();
			for(Node child : node.children) {
				Maybe<Object> object = this.execute(child, input);
				if(!object.has()) {
					return object;
				}
				objects.add(object.get());
			}
			return this.executors.get(node.function).execute(objects, input);
		}
	}
	
	public static interface Executor {
		public Maybe<Object> execute(List<Object> objects, Object input);
	}
}
