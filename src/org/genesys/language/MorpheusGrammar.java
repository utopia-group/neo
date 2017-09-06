package org.genesys.language;

import org.genesys.models.Example;
import org.genesys.models.Problem;
import org.genesys.type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yufeng on 5/26/17.
 */
public class MorpheusGrammar implements Grammar<AbstractType> {

    public AbstractType inputType;

    public AbstractType outputType;

    private List<InputType> inputTypes = new ArrayList<>();

    public MorpheusGrammar(AbstractType inputType, AbstractType outputType) {
        this.inputType = inputType;
        inputTypes.add(new InputType(0, inputType));
        this.outputType = outputType;
    }

    public MorpheusGrammar(Problem p) {
        assert !p.getExamples().isEmpty();
        Example example = p.getExamples().get(0);
        inputType = new TableType();

        //output is either an integer or list.
        this.outputType = new TableType();
    }


    private String getNeoString() {
        return "string";
    }

    private String getNeoList() {
        Integer[] dummy = {1, 2, 3};
        return Arrays.asList(dummy).toString();
    }

    @Override
    public AbstractType start() {
        return new InitType(this.outputType);

    }

    @Override
    public String getName() {
        return "MorpheusGrammar";
    }

    @Override
    public List<Production<AbstractType>> getProductions() {
        List<Production<AbstractType>> productions = new ArrayList<>();

        productions.add(new Production<>(new TableType(), "select", new TableType(), new ListType(new IntType())));
        productions.add(new Production<>(new TableType(), "group_by", new TableType(), new ListType(new IntType())));
        productions.add(new Production<>(new TableType(), "inner_join", new TableType(), new TableType()));
        productions.add(new Production<>(new TableType(), "gather", new TableType(), new ListType(new IntType())));
        productions.add(new Production<>(new TableType(), "spread", new TableType(), new ColIndexType(), new ColIndexType()));
        productions.add(new Production<>(new TableType(), "unite", new TableType(), new ColIndexType(), new ColIndexType()));
        productions.add(new Production<>(new TableType(), "summarize", new TableType(), new AggrType(), new ColIndexType()));
        productions.add(new Production<>(new TableType(), "separate", new TableType(), new ColIndexType()));
        productions.add(new Production<>(new TableType(), "filter", new TableType(),
                new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), new ColIndexType(), new IntType()));
        productions.add(new Production<>(new TableType(), "mutate", new TableType(), new ColIndexType(),
                new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), new ColIndexType()));

        //FunctionType
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "l(a,b).(/ a b)"));

        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(> a b)"));
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(< a b)"));
        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(== a b)"));


        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(> a b)", new IntType()));
        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(< a b)", new IntType()));
        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(== a b)", new IntType()));

        // Aggregator Type
        productions.add(new Production<>(new AggrType(), "mean"));
        productions.add(new Production<>(new AggrType(), "min"));
        productions.add(new Production<>(new AggrType(), "sum"));

        // IntType
        productions.add(new Production<>(new IntType(), "0"));
        productions.add(new Production<>(new IntType(), "1"));

        // ColIndexType
        productions.add(new Production<>(new ColIndexType(), "0"));
        productions.add(new Production<>(new ColIndexType(), "1"));

        //ListType
        productions.add(new Production<>(new ListType(new IntType()), getNeoList()));

        // String Type
        productions.add(new Production<>(new StringType(), getNeoString()));

        return productions;
    }

    @Override
    public List<Production<AbstractType>> productionsFor(AbstractType symbol) {
        return null;
    }

    @Override
    public AbstractType getOutputType() {
        return this.outputType;
    }

    @Override
    public List<Production<AbstractType>> getInputProductions() {
        List<Production<AbstractType>> productions = new ArrayList<>();

        ///Quick hack assuming we only have one input table.
        productions.add(new Production<>(new TableType(), "input0"));
        return productions;
    }
}
