package org.genesys.language;

import org.genesys.type.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 5/31/17.
 * Grammar for deepCoder.
 */
public class DeepCoderGrammar implements Grammar<AbstractType> {

    private AbstractType inputType;

    private AbstractType outputType;

    public DeepCoderGrammar(AbstractType inputType, AbstractType outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public AbstractType start() {
        return new AppToInputType(new FunctionType(this.inputType, this.outputType));
    }

    @Override
    public String getName() {
        return "DeepCoderGrammar";
    }

    @Override
    public List<Production<AbstractType>> getProductions() {
        return null;
    }

    @Override
    public List<Production<AbstractType>> productionsFor(AbstractType symbol) {
        List<Production<AbstractType>> productions = new ArrayList<>();
        if (symbol instanceof AppToInputType) {
            AppToInputType type = (AppToInputType) symbol;
            productions.add(new Production<>(symbol, "apply_to_input", type.functionType));
        } else if (symbol instanceof BoolType) {
            productions.add(new Production<>(symbol, "true"));
            productions.add(new Production<>(symbol, "false"));
        } else if (symbol instanceof IntType) {
            productions.add(new Production<>(symbol, "0"));
            productions.add(new Production<>(symbol, "1"));
            productions.add(new Production<>(symbol, "+1", symbol));
            productions.add(new Production<>(symbol, "-1", symbol));
            productions.add(new Production<>(symbol, "*3", symbol));
            productions.add(new Production<>(symbol, "maximum", new ListType(new IntType())));

        } else if (symbol instanceof ListType) {
            ListType type = (ListType) symbol;
            productions.add(new Production<>(symbol, "emp"));
            productions.add(new Production<>(symbol, "cons", type.type, symbol));
        } else if (symbol instanceof PairType) {
            PairType type = (PairType) symbol;
            productions.add(new Production<>(symbol, "pair", type.firstType, type.secondType));
        } else if (symbol instanceof FunctionType) {
            FunctionType type = (FunctionType) symbol;
            // map (I -> O) ::= (List<I> -> List<O>)
            if (type.inputType instanceof ListType && type.outputType instanceof ListType) {
                AbstractType I = ((ListType) type.inputType).type;
                AbstractType O = ((ListType) type.outputType).type;
                productions.add(new Production<>(symbol, "map", new FunctionType(I, O)));
            }
            // filter (T -> Boolean) ::= (List<T> -> List<T>)
            if (type.inputType instanceof ListType && type.outputType instanceof ListType
                    && type.inputType.equals(type.outputType)) {
                AbstractType T = ((ListType) type.inputType).type;
                productions.add(new Production<>(symbol, "filter", new FunctionType(T, new BoolType())));
            }
            // zipWith (List<T> -> List<T> -> int -> int -> int -> List<T>)
            if (type.inputType instanceof PairType && ((PairType) type.inputType).firstType instanceof ListType
                    && ((PairType) type.inputType).secondType instanceof ListType
                    && type.outputType instanceof ListType) {
                AbstractType input1 = ((PairType) type.inputType).firstType;
                assert input1 instanceof ListType;
                AbstractType I = ((ListType) input1).type;
                AbstractType O = ((ListType) type.outputType).type;
                productions.add(new Production<>(symbol, "zipWith", new FunctionType(new PairType(I, O), O)));
            }
            // l(a,b).(+ a b) ::= ((Integer, Integer) -> Integer)
            // l(a,b).(* a b) ::= ((Integer, Integer) -> Integer)
            if (type.inputType instanceof PairType && ((PairType) type.inputType).firstType instanceof IntType
                    && ((PairType) type.inputType).secondType instanceof IntType && type.outputType instanceof IntType) {
                productions.add(new Production<>(symbol, "l(a,b).(+ a b)"));
                productions.add(new Production<>(symbol, "l(a,b).(* a b)"));
                productions.add(new Production<>(symbol, "l(a,b).(% a b)"));
            }
            // l(a,b).(> a b) ::= ((Integer, Integer) -> Boolean)
            // l(a,b).(< a b) ::= ((Integer, Integer) -> Boolean)
            // l(a,b).(>= a b) ::= ((Integer, Integer) -> Boolean)
            // l(a,b).(<= a b) ::= ((Integer, Integer) -> Boolean)
            if (type.inputType instanceof PairType && ((PairType) type.inputType).firstType instanceof IntType
                    && ((PairType) type.inputType).secondType instanceof IntType && type.outputType instanceof BoolType) {
                productions.add(new Production<>(symbol, "l(a,b).(> a b)"));
                productions.add(new Production<>(symbol, "l(a,b).(< a b)"));
                productions.add(new Production<>(symbol, "l(a,b).(== a b)"));
            }
            // l(a,b).(|| a b) ::= ((Boolean, Boolean) -> Boolean)
            // l(a,b).(&& a b) ::= ((Boolean, Boolean) -> Boolean)
            if (type.inputType instanceof PairType && ((PairType) type.inputType).firstType instanceof BoolType
                    && ((PairType) type.inputType).secondType instanceof BoolType && type.outputType instanceof BoolType) {
                productions.add(new Production<>(symbol, "l(a,b).(|| a b)"));
                productions.add(new Production<>(symbol, "l(a,b).(&& a b)"));
            }
            // l(a).(+ a b) (Integer) ::= (Integer -> Integer)
            // l(a).(* a b) (Integer) ::= (Integer -> Integer)
            if (type.inputType instanceof IntType && type.outputType instanceof IntType) {
                productions.add(new Production<>(symbol, "l(a).(+ a b)", new IntType()));
                productions.add(new Production<>(symbol, "l(a).(* a b)", new IntType()));
                productions.add(new Production<>(symbol, "l(a).(% a b)", new IntType()));
            }
            // l(a).(> a b) (Integer) ::= (Integer -> Boolean)
            // l(a).(< a b) (Integer) ::= (Integer -> Boolean)
            if (type.inputType instanceof IntType && type.outputType instanceof BoolType) {
                productions.add(new Production<>(symbol, "l(a).(> a b)", new IntType()));
                productions.add(new Production<>(symbol, "l(a).(< a b)", new IntType()));
                productions.add(new Production<>(symbol, "l(a).(== a b)", new IntType()));
            }
            // l(a).(|| a b) (Integer) ::= (Boolean -> Boolean)
            // l(a).(&& a b) (Integer) ::= (Boolean -> Boolean)
            if (type.inputType instanceof PairType && ((PairType) type.inputType).firstType instanceof BoolType
                    && ((PairType) type.inputType).secondType instanceof BoolType && type.outputType instanceof BoolType) {
                productions.add(new Production<>(symbol, "l(a).(|| a b)", new BoolType()));
                productions.add(new Production<>(symbol, "l(a).(&& a b)", new BoolType()));
            }
            // l(a).(- a) ::= (Integer -> Integer)
            if (type.inputType instanceof IntType && type.outputType instanceof IntType) {
                productions.add(new Production<>(symbol, "l(a).(- a)"));
            }
        }
        return productions;
    }
}
