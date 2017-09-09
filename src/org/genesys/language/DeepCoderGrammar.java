package org.genesys.language;

import org.genesys.models.Example;
import org.genesys.models.Problem;
import org.genesys.type.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufeng on 5/31/17.
 * Grammar for deepCoder.
 */
public class DeepCoderGrammar implements Grammar<AbstractType> {

    public AbstractType inputType;

    public AbstractType outputType;

    private List<InputType> inputTypes = new ArrayList<>();

    public DeepCoderGrammar(AbstractType inputType, AbstractType outputType) {
        this.inputType = inputType;
        inputTypes.add(new InputType(0, inputType));
        this.outputType = outputType;
    }

    public DeepCoderGrammar(Problem p) {
        assert !p.getExamples().isEmpty();
        Example example = p.getExamples().get(0);
        List input = example.getInput();
        for (int i = 0; i < input.size(); i++) {
            Object elem = input.get(i);
            InputType in;
            if (elem instanceof List)
                in = new InputType(i, new ListType(new IntType()));
            else
                in = new InputType(i, new IntType());

        /* dynamically add input to grammar. */
            addInput(in);
        }
        Object output = example.getOutput();
        //output is either an integer or list.
        if (output instanceof List) {
            this.outputType = new ListType(new IntType());
        } else {
            this.outputType = new IntType();
        }
    }

    public void addInput(InputType in) {
        inputTypes.add(in);
    }

    @Override
    public AbstractType start() {
        return new InitType(this.outputType);
    }

    @Override
    public String getName() {
        return "DeepCoderGrammar";
    }

    public AbstractType getOutputType() {
        return this.outputType;
    }

    @Override
    public List<Production<AbstractType>> getInputProductions() {
        List<Production<AbstractType>> productions = new ArrayList<>();

        for (InputType input : inputTypes) {
            if (input.getType() instanceof IntType)
                productions.add(new Production<>(new IntType(), "input" + input.getIndex()));
            else if (input.getType() instanceof ListType)
                productions.add(new Production<>(new ListType(new IntType()), "input" + input.getIndex()));
            else if (input.getType() instanceof BoolType)
                productions.add(new Production<>(new BoolType(), "input" + input.getIndex()));
            else
                assert (false);
        }

        return productions;
    }

    @Override
    public List<Production<AbstractType>> getProductions() {
        List<Production<AbstractType>> productions = new ArrayList<>();
        // IntType
//        productions.add(new Production<>(new IntType(), "0"));
//        productions.add(new Production<>(new IntType(), "1"));

        productions.add(new Production<>(new IntType(), "MAXIMUM", new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "MINIMUM", new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "SUM", new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "HEAD", new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "LAST", new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "COUNT", new FunctionType(new IntType(), new BoolType()),
                new ListType(new IntType())));
        productions.add(new Production<>(new IntType(), "ACCESS", new ListType(new IntType()), new IntType()));

        // ListType -- only considering lists of IntType
        productions.add(new Production<>(new ListType(new IntType()), "FILTER", new FunctionType(new IntType(), new BoolType()),
                new ListType(new IntType())));
        productions.add(new Production<>(new ListType(new IntType()), "MAP", new FunctionType(new IntType(), new IntType()),
                new ListType(new IntType())));
        productions.add(new Production<>(new ListType(new IntType()), "ZIPWITH", new FunctionType(new PairType(new IntType(), new IntType()), new IntType()),
                new ListType(new IntType()), new ListType(new IntType())));
        productions.add(new Production<>(new ListType(new IntType()), "SORT", new ListType(new IntType())));
        productions.add(new Production<>(new ListType(new IntType()), "REVERSE", new ListType(new IntType())));
        productions.add(new Production<>(new ListType(new IntType()), "SCANL1", new FunctionType(new PairType(new IntType(),
                new IntType()), new IntType()), new ListType(new IntType())));


        productions.add(new Production<>(new ListType(new IntType()), "TAKE", new ListType(new IntType()), new IntType()));
        productions.add(new Production<>(new ListType(new IntType()), "DROP", new ListType(new IntType()), new IntType()));


        //FunctionType
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "+"));
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "-"));
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "*"));

        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "MIN"));
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "MAX"));


        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "MUL3"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "MUL4"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DIV3"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DIV4"));

        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "INC"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DEC"));

        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SHL"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SHR"));

        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SQR"));
        productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "doNEG"));


        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isPOS"));
        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isNEG"));
        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isODD"));
        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isEVEN"));

        return productions;
    }

    @Override
    public List<Production<AbstractType>> productionsFor(AbstractType symbol) {
        List<Production<AbstractType>> productions = new ArrayList<>();
        if (symbol instanceof InitType) {
            InitType type = (InitType) symbol;
            productions.add(new Production<>(symbol, "root", type.goalType));
        } else if (symbol instanceof IntType) {
            productions.add(new Production<>(new IntType(), "MAXIMUM", new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "MINIMUM", new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "SUM", new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "HEAD", new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "LAST", new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "COUNT", new FunctionType(new IntType(), new BoolType()),
                    new ListType(new IntType())));
            productions.add(new Production<>(new IntType(), "ACCESS", new ListType(new IntType()), new IntType()));

        } else if (symbol instanceof ListType) {
            // ListType -- only considering lists of IntType
            productions.add(new Production<>(new ListType(new IntType()), "FILTER", new FunctionType(new IntType(), new BoolType()),
                    new ListType(new IntType())));
            productions.add(new Production<>(new ListType(new IntType()), "MAP", new FunctionType(new IntType(), new IntType()),
                    new ListType(new IntType())));
            productions.add(new Production<>(new ListType(new IntType()), "ZIPWITH", new FunctionType(new PairType(new IntType(), new IntType()), new IntType()),
                    new ListType(new IntType()), new ListType(new IntType())));
            productions.add(new Production<>(new ListType(new IntType()), "SORT", new ListType(new IntType())));
            productions.add(new Production<>(new ListType(new IntType()), "REVERSE", new ListType(new IntType())));
            productions.add(new Production<>(new ListType(new IntType()), "SCANL1", new FunctionType(new PairType(new IntType(),
                    new IntType()), new IntType()), new ListType(new IntType())));


            productions.add(new Production<>(new ListType(new IntType()), "TAKE", new ListType(new IntType()), new IntType()));
            productions.add(new Production<>(new ListType(new IntType()), "DROP", new ListType(new IntType()), new IntType()));
        } else if (symbol instanceof FunctionType) {
            FunctionType type = (FunctionType) symbol;
            if ((type.inputType instanceof IntType) && (type.outputType instanceof BoolType)) {
                productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isPOS", new IntType()));
                productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isNEG", new IntType()));
                productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isODD", new IntType()));
                productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "isEVEN", new IntType()));
            }

            if ((type.inputType instanceof IntType) && (type.outputType instanceof IntType)) {
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "MUL3"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "MUL4"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DIV3"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DIV4"));

                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "INC"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "DEC"));

                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SHL"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SHR"));

                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "SQR"));
                productions.add(new Production<>(new FunctionType(new IntType(), new IntType()), "doNEG"));

            }

            if (type.inputType instanceof PairType) {
                //FunctionType
                productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "+"));
                productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "-"));
                productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "*"));

                productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "MIN"));
                productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "MAX"));
            }
        }

        /* Handle inputs */
        for (InputType input : inputTypes) {
            boolean flag1 = (symbol instanceof IntType) && (input.getType() instanceof IntType);
            boolean flag2 = (symbol instanceof ListType) && (input.getType() instanceof ListType);
            if (flag1 || flag2) {
                productions.add(new Production<>(symbol, "input" + input.getIndex()));
            }
        }
        return productions;
    }
}
