package synth.instance.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synth.language.GrammarUtils.Grammar;
import synth.language.GrammarUtils.Production;
import synth.language.InterpreterUtils.Executor;
import synth.language.InterpreterUtils.Interpreter;
import synth.utils.Utils.Maybe;

public class ListInstance {
	public static interface Type {}
	public static class BooleanType implements Type {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof BooleanType;
		}
		@Override
		public int hashCode() {
			return 0;
		}
		@Override
		public String toString() {
			return "Boolean";
		}
	}
	public static class IntegerType implements Type {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof IntegerType;
		}
		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		public String toString() {
			return "Integer";
		}
	}
	public static class ListType implements Type {
		public final Type type;
		public ListType(Type type) {
			this.type = type;
		}
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof ListType)) {
				return false;
			}
			return this.type.equals(((ListType)obj).type);
		}
		@Override
		public int hashCode() {
			return 5*this.type.hashCode() + 1;
		}
		@Override
		public String toString() {
			return "List<" + this.type.toString() + ">";
		}
	}
	public static class PairType implements Type {
		public final Type firstType;
		public final Type secondType;
		public PairType(Type firstType, Type secondType) {
			this.firstType = firstType;
			this.secondType = secondType;
		}
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof PairType)) {
				return false;
			}
			PairType type = (PairType)obj;
			return this.firstType.equals(type.firstType) && this.secondType.equals(type.secondType);
		}
		@Override
		public int hashCode() {
			return 5*(5*this.firstType.hashCode() + this.secondType.hashCode()) + 2;
		}
		@Override
		public String toString() {
			return "(" + this.firstType.toString() + ", " + this.secondType.toString() + ")";
		}
	}
	public static class FunctionType implements Type {
		public final Type inputType;
		public final Type outputType;
		public FunctionType(Type inputType, Type outputType) {
			this.inputType = inputType;
			this.outputType = outputType;
		}
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof FunctionType)) {
				return false;
			}
			FunctionType type = (FunctionType)obj;
			return this.inputType.equals(type.inputType) && this.outputType.equals(type.outputType);
		}
		@Override
		public int hashCode() {
			return 5*(5*this.inputType.hashCode() + this.outputType.hashCode()) + 3;
		}
		@Override
		public String toString() {
			return "(" + this.inputType.toString() + " -> " + this.outputType.toString() + ")";
		}
	}
	public static class ApplicationToInputType implements Type {
		public final FunctionType functionType;
		public ApplicationToInputType(FunctionType functionType) {
			this.functionType = functionType;
		}
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof ApplicationToInputType)) {
				return false;
			}
			ApplicationToInputType type = (ApplicationToInputType)obj;
			return this.functionType.equals(type.functionType);
		}
		@Override
		public int hashCode() {
			return 5*this.functionType.hashCode() + 4;
		}
	}
	
	public static Grammar<Type> getListGrammar(Type inputType, Type outputType) {
		return new ListGrammar(inputType, outputType);
	}
	
	private static class ListGrammar implements Grammar<Type> {
		private final Type inputType;
		private final Type outputType;
		private ListGrammar(Type inputType, Type outputType) {
			this.inputType = inputType;
			this.outputType = outputType;
		}
		@Override
		public Type start() {
			return new ApplicationToInputType(new FunctionType(this.inputType, this.outputType));
		}
		@Override
		public List<Production<Type>> productionsFor(Type symbol) {
			List<Production<Type>> productions = new ArrayList<Production<Type>>();
			if(symbol instanceof ApplicationToInputType) {
				ApplicationToInputType type = (ApplicationToInputType)symbol;
				productions.add(new Production<Type>("apply_to_input", type.functionType));
			} else if(symbol instanceof BooleanType) {
				productions.add(new Production<Type>("true"));
				productions.add(new Production<Type>("false"));
			} else if(symbol instanceof IntegerType) {
				productions.add(new Production<Type>("0"));
				productions.add(new Production<Type>("1"));
				productions.add(new Production<Type>("+1", symbol));
				productions.add(new Production<Type>("-", new IntegerType()));
			} else if(symbol instanceof ListType) {
				ListType type = (ListType)symbol;
				productions.add(new Production<Type>("emp"));
				productions.add(new Production<Type>("cons", type.type, symbol));
			} else if(symbol instanceof PairType) {
				PairType type = (PairType)symbol;
				productions.add(new Production<Type>("pair", type.firstType, type.secondType));
			} else if(symbol instanceof FunctionType) {
				FunctionType type = (FunctionType)symbol;
				// map (I -> O) ::= (List<I> -> List<O>)
				if(type.inputType instanceof ListType && type.outputType instanceof ListType) {
					Type I = ((ListType)type.inputType).type;
					Type O = ((ListType)type.outputType).type;
					productions.add(new Production<Type>("map", new FunctionType(I, O)));
				}
				// filter (T -> Boolean) ::= (List<T> -> List<T>)
				if(type.inputType instanceof ListType && type.outputType instanceof ListType && type.inputType.equals(type.outputType)) {
					Type T = ((ListType)type.inputType).type;
					productions.add(new Production<Type>("filter", new FunctionType(T, new BooleanType())));
				}
				// fold ((I, O) -> O, O) ::= (List<I> -> O)
				if(type.inputType instanceof ListType) {
					Type I = ((ListType)type.inputType).type;
					Type O = type.outputType;
					productions.add(new Production<Type>("foldLeft", new FunctionType(new PairType(I, O), O), O));
					productions.add(new Production<Type>("foldRight", new FunctionType(new PairType(I, O), O), O));
				}
				// l(a,x).(cons a x) ::= ((T, List<T>) -> List<T>)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).secondType instanceof ListType && type.outputType instanceof ListType) {
					Type T = ((ListType)((PairType)type.inputType).secondType).type;
					if(T.equals(((PairType)type.inputType).firstType) && T.equals(((ListType)type.outputType).type)) {
						productions.add(new Production<Type>("l(a,x).(cons a x)"));
					}
				}
				// l(x,a).(cons a x) ::= ((List<T>, T) -> List<T>)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).firstType instanceof ListType && type.outputType instanceof ListType) {
					Type T = ((ListType)((PairType)type.inputType).firstType).type;
					if(T.equals(((PairType)type.inputType).secondType) && T.equals(((ListType)type.outputType).type)) {
						productions.add(new Production<Type>("l(x,a).(cons a x)"));
					}
				}
				// l(a).(cons a x) (List<T>) ::= (T -> List<T>)
				if(type.outputType instanceof ListType && type.inputType.equals(((ListType)type.outputType).type)) {
					Type T = type.outputType;
					productions.add(new Production<Type>("l(a).(cons a x)", T));
				}
				// l(x).(cons a x) (T) ::= (List<T> -> List<T>)
				if(type.inputType instanceof ListType && type.outputType instanceof ListType && type.inputType.equals(type.outputType)) {
					Type T = ((ListType)type.inputType).type;
					productions.add(new Production<Type>("l(x).(cons a x)", T));
				}
				// l(a,b).(+ a b) ::= ((Integer, Integer) -> Integer)
				// l(a,b).(* a b) ::= ((Integer, Integer) -> Integer)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).firstType instanceof IntegerType && ((PairType)type.inputType).secondType instanceof IntegerType && type.outputType instanceof IntegerType) {
					productions.add(new Production<Type>("l(a,b).(+ a b)"));
					productions.add(new Production<Type>("l(a,b).(* a b)"));
				}
				// l(a,b).(> a b) ::= ((Integer, Integer) -> Boolean)
				// l(a,b).(< a b) ::= ((Integer, Integer) -> Boolean)
				// l(a,b).(>= a b) ::= ((Integer, Integer) -> Boolean)
				// l(a,b).(<= a b) ::= ((Integer, Integer) -> Boolean)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).firstType instanceof IntegerType && ((PairType)type.inputType).secondType instanceof IntegerType && type.outputType instanceof BooleanType) {
					productions.add(new Production<Type>("l(a,b).(> a b)"));
					productions.add(new Production<Type>("l(a,b).(< a b)"));
					productions.add(new Production<Type>("l(a,b).(>= a b)"));
					productions.add(new Production<Type>("l(a,b).(<= a b)"));
				}
				// l(a,b).(|| a b) ::= ((Boolean, Boolean) -> Boolean)
				// l(a,b).(&& a b) ::= ((Boolean, Boolean) -> Boolean)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).firstType instanceof BooleanType && ((PairType)type.inputType).secondType instanceof BooleanType && type.outputType instanceof BooleanType) {
					productions.add(new Production<Type>("l(a,b).(|| a b)"));
					productions.add(new Production<Type>("l(a,b).(&& a b)"));
				}
				// l(a).(+ a b) (Integer) ::= (Integer -> Integer)
				// l(a).(* a b) (Integer) ::= (Integer -> Integer)
				if(type.inputType instanceof IntegerType && type.outputType instanceof IntegerType) {
					productions.add(new Production<Type>("l(a).(+ a b)", new IntegerType()));
					productions.add(new Production<Type>("l(a).(* a b)", new IntegerType()));
				}
				// l(a).(> a b) (Integer) ::= (Integer -> Boolean)
				// l(a).(< a b) (Integer) ::= (Integer -> Boolean)
				// l(a).(>= a b) (Integer) ::= (Integer -> Boolean)
				// l(a).(<= a b) (Integer) ::= (Integer -> Boolean)
				if(type.inputType instanceof IntegerType && type.outputType instanceof BooleanType) {
					productions.add(new Production<Type>("l(a).(> a b)", new IntegerType()));
					productions.add(new Production<Type>("l(a).(< a b)", new IntegerType()));
					productions.add(new Production<Type>("l(a).(>= a b)", new IntegerType()));
					productions.add(new Production<Type>("l(a).(<= a b)", new IntegerType()));
				}
				// l(a).(|| a b) (Integer) ::= (Boolean -> Boolean)
				// l(a).(&& a b) (Integer) ::= (Boolean -> Boolean)
				if(type.inputType instanceof PairType && ((PairType)type.inputType).firstType instanceof BooleanType && ((PairType)type.inputType).secondType instanceof BooleanType && type.outputType instanceof BooleanType) {
					productions.add(new Production<Type>("l(a).(|| a b)", new BooleanType()));
					productions.add(new Production<Type>("l(a).(&& a b)", new BooleanType()));
				}
				// l(a).(- a) ::= (Integer -> Integer)
				if(type.inputType instanceof IntegerType && type.outputType instanceof IntegerType) {
					productions.add(new Production<Type>("l(a).(- a)"));
				}
				// l(a).(~ a) ::= (Boolean -> Boolean)
				if(type.inputType instanceof BooleanType && type.outputType instanceof BooleanType) {
					productions.add(new Production<Type>("l(a).(~ a)"));
				}
			}
			return productions;
		}
	}

	public static interface LinkedList {}
	public static class Emp implements LinkedList {
		@Override
		public String toString() {
			return "emp";
		}
	}
	public static class Cons implements LinkedList {
		public final Object obj;
		public final LinkedList list;
		public Cons(Object obj, LinkedList list) {
			this.obj = obj;
			this.list = list;
		}
		@Override
		public String toString() {
			return "(cons " + this.obj.toString() + " " + this.list.toString() + ")";
		}
	}
	public static class Pair {
		public final Object first;
		public final Object second;
		public Pair(Object first, Object second) {
			this.first = first;
			this.second = second;
		}
		@Override
		public String toString() {
			return "(pair " + this.first.toString() + " " + this.second.toString() + ")";
		}
	}
	public static interface Binop {
		public Object apply(Object first, Object second);
	}
	public static interface Unop {
		public Object apply(Object obj);
	}
	public static class ConsBinop implements Binop {
		public Object apply(Object first, Object second) {
			return new Cons(first, (LinkedList)second);
		}
		public String toString() {
			return "l(a,x).(cons a x)";
		}
	}
	public static class ConsRevBinop implements Binop {
		public Object apply(Object first, Object second) {
			return new Cons(second, (LinkedList)first);
		}
		public String toString() {
			return "l(x,a).(cons a x)";
		}
	}
	public static class ConsFirstUnop implements Unop {
		private final LinkedList list;
		public ConsFirstUnop(LinkedList list) {
			this.list = list;
		}
		public Object apply(Object obj) {
			return new Cons(obj, this.list);
		}
		public String toString() {
			return "l(a).(cons a " + this.list.toString() + ")";
		}
	}
	public static class ConsSecondUnop implements Unop {
		private final Object obj;
		public ConsSecondUnop(Object obj) {
			this.obj = obj;
		}
		public Object apply(Object obj) {
			return new Cons(this.obj, (LinkedList)obj);
		}
		public String toString() {
			return "l(x).(cons " + this.obj.toString() + " x)";
		}
	}
	public static class PrimitiveBinop implements Binop {
		private final String op;
		public PrimitiveBinop(String op) {
			this.op = op;
		}
		public Object apply(Object first, Object second) {
			if(this.op.equals("+")) {
				return (int)first + (int)second;
			} else if(this.op.equals("*")) {
				return (int)first * (int)second;
			} else if(this.op.equals(">")) {
				return (int)first > (int)second;
			} else if(this.op.equals(">=")) {
				return (int)first >= (int)second;
			} else if(this.op.equals("<")) {
				return (int)first < (int)second;
			} else if(this.op.equals("<=")) {
				return (int)first <= (int)second;
			} else if(this.op.equals("||")) {
				return (boolean)first || (boolean)second;
			} else if(this.op.equals("&&")) {
				return (boolean)first && (boolean)second;
			} else if(this.op.equals("-")) {
				return -(int)first;
			} else if(this.op.equals("~")) {
				return !(boolean)first;
			} else {
				throw new RuntimeException();
			}
		}
		@Override
		public String toString() {
			return "l(a,b).(" + this.op + " a b)";
		}
	}
	public static class PrimitiveUnop implements Unop {
		private final PrimitiveBinop op;
		private final Object val;
		public PrimitiveUnop(String op, Object val) {
			this.op = new PrimitiveBinop(op);
			this.val = val;
		}
		public Object apply(Object obj) {
			return this.op.apply(obj, this.val);
		}
		@Override
		public String toString() {
			if(op.equals("-") || op.equals("~")) {
				return "l(a).(" + this.op + " a)";
			} else {
				return "l(a).(" + this.op + " (" + this.val.toString() + ") b)";
			}
		}
	}
	public static class MapUnop implements Unop {
		private final Unop unop;
		public MapUnop(Unop unop) {
			this.unop = unop;
		}
		public Object apply(Object obj) {
			LinkedList list = (LinkedList)obj;
			if(list instanceof Emp) {
				return list;
			} else {
				Cons cons = (Cons)list;
				return new Cons(this.unop.apply(cons.obj), (LinkedList)this.apply(cons.list));
			}
		}
		public String toString() {
			return "l(x).(map " + this.unop.toString() + " x)";
		}
	}
	public static class FilterUnop implements Unop {
		private final Unop unop;
		public FilterUnop(Unop unop) {
			this.unop = unop;
		}
		public Object apply(Object obj) {
			LinkedList list = (LinkedList)obj;
			if(list instanceof Emp) {
				return list;
			} else {
				Cons cons = (Cons)list;
				if((boolean)this.unop.apply(cons.obj)) {
					return new Cons(cons.obj, (LinkedList)this.apply(cons.list));
				} else {
					return this.apply(cons.list);
				}
			}
		}
		public String toString() {
			return "l(x).(filter " + this.unop.toString() + " x)";
		}
	}
	public static class FoldLeftUnop implements Unop {
		private final Binop binop;
		private final Object val;
		public FoldLeftUnop(Binop binop, Object val) {
			this.binop = binop;
			this.val = val;
		}
		public Object apply(Object obj) {
			return this.applyRec(obj, this.val);
		}
		private Object applyRec(Object first, Object second) {
			LinkedList list = (LinkedList)first;
			if(list instanceof Emp) {
				return second;
			} else {
				Cons cons = (Cons)list;
				return this.applyRec(cons.list, this.binop.apply(cons.obj, second));
			}
		}
		public String toString() {
			return "l(x).(foldLeft " + this.binop.toString() + " x)";
		}
	}
	public static class FoldRightUnop implements Unop {
		private final Binop binop;
		private final Object val;
		public FoldRightUnop(Binop binop, Object val) {
			this.binop = binop;
			this.val = val;
		}
		public Object apply(Object obj) {
			return this.applyRec(obj, this.val);
		}
		private Object applyRec(Object first, Object second) {
			LinkedList list = (LinkedList)first;
			if(list instanceof Emp) {
				return second;
			} else {
				Cons cons = (Cons)list;
				return this.binop.apply(cons.obj, this.applyRec(cons.list, second));
			}
		}
		public String toString() {
			return "l(x).(foldRight " + this.binop.toString() + " x)";
		}
	}
	
	public static Interpreter getListInterpreter() {
		Map<String,Executor> executors = new HashMap<String,Executor>();
		executors.put("apply_to_input", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(((Unop)objects.get(0)).apply(input)); }});
		executors.put("true", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(true); }});
		executors.put("false", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(false); }});
		executors.put("0", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(0); }});
		executors.put("1", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(1); }});
		executors.put("+1", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>((int)objects.get(0)+1); }});
		executors.put("-", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(-(int)objects.get(0)); }});
		executors.put("emp", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new Emp()); }});
		executors.put("cons", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new Cons(objects.get(0), (LinkedList)objects.get(1))); }});
		executors.put("pair", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new Pair(objects.get(0), objects.get(1))); }});
		executors.put("map", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new MapUnop((Unop)objects.get(0))); }});
		executors.put("filter", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new FilterUnop((Unop)objects.get(0))); }});
		executors.put("foldLeft", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new FoldLeftUnop((Binop)objects.get(0), objects.get(1))); }});
		executors.put("foldRight", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new FoldRightUnop((Binop)objects.get(0), objects.get(1))); }});
		executors.put("l(a,x).(cons a x)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new ConsBinop()); }});
		executors.put("l(x,a).(cons a x)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new ConsRevBinop()); }});
		executors.put("l(a).(cons a x)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new ConsFirstUnop((LinkedList)objects.get(0))); }});
		executors.put("l(x).(cons a x)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new ConsSecondUnop(objects.get(0))); }});
		executors.put("l(a,b).(+ a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("+")); }});
		executors.put("l(a,b).(* a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("*")); }});
		executors.put("l(a,b).(> a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop(">")); }});
		executors.put("l(a,b).(< a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("<")); }});
		executors.put("l(a,b).(>= a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop(">=")); }});
		executors.put("l(a,b).(<= a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("<=")); }});
		executors.put("l(a,b).(|| a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("||")); }});
		executors.put("l(a,b).(&& a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveBinop("&&")); }});
		executors.put("l(a).(+ a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("+", objects.get(0))); }});
		executors.put("l(a).(* a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("*", objects.get(0))); }});
		executors.put("l(a).(> a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop(">", objects.get(0))); }});
		executors.put("l(a).(< a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("<", objects.get(0))); }});
		executors.put("l(a).(>= a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop(">=", objects.get(0))); }});
		executors.put("l(a).(<= a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("<=", objects.get(0))); }});
		executors.put("l(a).(|| a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("||", objects.get(0))); }});
		executors.put("l(a).(&& a b)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("&&", objects.get(0))); }});
		executors.put("l(a).(- a)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("-", null)); }});
		executors.put("l(a).(~ a)", new Executor() {
			public Maybe<Object> execute(List<Object> objects, Object input) {
				return new Maybe<Object>(new PrimitiveUnop("~", null)); }});
		return new Interpreter(executors);
	}
}
