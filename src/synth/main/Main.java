package synth.main;

import java.util.Random;

import synth.instance.list.ListInstance;
import synth.instance.list.ListInstance.Cons;
import synth.instance.list.ListInstance.Emp;
import synth.instance.list.ListInstance.IntegerType;
import synth.instance.list.ListInstance.LinkedList;
import synth.instance.list.ListInstance.ListType;
import synth.instance.list.ListInstance.Type;
import synth.language.GrammarUtils.Grammar;
import synth.language.GrammarUtils.Node;
import synth.language.InterpreterUtils.Interpreter;
import synth.sampler.SamplerUtils.RandomNodeSampler;
import synth.utils.Utils.Maybe;

public class Main {
	public static void main(String[] args) {
		Grammar<Type> grammar = ListInstance.getListGrammar(new ListType(new IntegerType()), new ListType(new IntegerType()));
		Interpreter interpreter = ListInstance.getListInterpreter();
		//int seed = -981645466;
		int seed = new Random().nextInt();
		Random random = new Random(seed);
		RandomNodeSampler sampler = new RandomNodeSampler(random);
		LinkedList list = new Cons(0, new Cons(1, new Cons(2, new Emp())));
		Node node = sampler.sample(grammar, new Maybe<Node>(), grammar.start());
		System.out.println("SEED: " + seed);
		System.out.println("PROGRAM: " + node.toString());
		System.out.println("INPUT: " + list);
		System.out.println("OUTPUT: " + interpreter.execute(node, list).get());
	}
}
