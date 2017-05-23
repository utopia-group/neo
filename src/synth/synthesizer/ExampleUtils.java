package synth.synthesizer;

import synth.language.GrammarUtils.Node;
import synth.language.InterpreterUtils.Interpreter;
import synth.synthesizer.SynthesizerUtils.Checker;
import synth.utils.Utils.Maybe;

public class ExampleUtils {
	public static class ExampleChecker implements Checker<Example> {
		private final Interpreter interpreter;
		public ExampleChecker(Interpreter interpreter) {
			this.interpreter = interpreter;
		}
		@Override
		public boolean check(Example specification, Node node) {
			Maybe<Object> output = this.interpreter.execute(node, specification.input);
			return output.has() && output.equals(specification.output);
		}
	}
	
	public static class Example {
		public final Object input;
		public final Object output;
		public Example(Object input, Object output) {
			this.input = input;
			this.output = output;
		}
	}
}
