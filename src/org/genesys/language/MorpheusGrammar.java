package org.genesys.language;

import krangl.DataFrame;
import org.genesys.models.Example;
import org.genesys.models.Problem;
import org.genesys.type.*;
import org.genesys.utils.MorpheusUtil;

import java.util.*;

/**
 * Created by yufeng on 9/6/17.
 */
public class MorpheusGrammar implements Grammar<AbstractType> {

    public AbstractType inputType;

    public AbstractType outputType;

    // maximum column number we need to consider
    private int maxCol = 5;

    private List<Production<AbstractType>> initProductions = new ArrayList<>();

    private List<Production<AbstractType>> inputProductions = new ArrayList<>();

    public MorpheusGrammar(Problem p) {
        assert !p.getExamples().isEmpty();
        //FIXME: assume we always only have one example in table domain.
        Example example = p.getExamples().get(0);
        // Rules for inputs
        inputType = new TableType();
        Set constSet = new HashSet();
        for (int i = 0; i < example.getInput().size(); i++) {
            DataFrame input = (DataFrame) example.getInput().get(i);
            //initProductions.add(new Production<>(new TableType(), "input" + i));
            inputProductions.add(new Production<>(new TableType(), "input" + i));
            for (Map<String, Object> row : input.getRows()) {
                constSet.addAll(row.values());
            }
        }

        // Rules for int constants
        for (Object o : constSet) {
            if (o instanceof Number) {
                Production prod = new Production<>(new IntType(), o.toString());
                prod.setValue(o);
                initProductions.add(prod);
            }
        }

        // Rule for output.
        this.outputType = new TableType();

        // Rules for column list
        List<Integer> allCols = new ArrayList<>();
        for (int i = 0; i < maxCol; i++) {
            // Rule for possible column name.
            Production prod = new Production<>(new ColIndexType(), i + "");
            prod.setValue(i);
            initProductions.add(prod);
            allCols.add(i);
        }
        List<Set<Integer>> cols = MorpheusUtil.getInstance().getSubsets(allCols, 1);
        cols.addAll(MorpheusUtil.getInstance().getSubsets(allCols, 2));
        for (Set<Integer> col : cols) {
            Production prod = new Production<>(new ListType(new IntType()), col.toString());
            prod.setValue(new ArrayList(col));
            initProductions.add(prod);
        }
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
        productions.addAll(initProductions);

//        productions.add(new Production<>(new TableType(), "select", new TableType(), new ListType(new IntType())));
//        productions.add(new Production<>(new TableType(), "group_by", new TableType(), new ListType(new IntType())));
//        productions.add(new Production<>(new TableType(), "inner_join", new TableType(), new TableType()));
        productions.add(new Production<>(true, new TableType(), "gather", new TableType(), new ListType(new IntType())));
        productions.add(new Production<>(true, new TableType(), "spread", new TableType(), new ColIndexType(), new ColIndexType()));
        productions.add(new Production<>(true, new TableType(), "unite", new TableType(), new ColIndexType(), new ColIndexType()));
//        productions.add(new Production<>(new TableType(), "summarise", new TableType(), new AggrType(), new ColIndexType()));
//        productions.add(new Production<>(new TableType(), "separate", new TableType(), new ColIndexType()));
//        productions.add(new Production<>(new TableType(), "filter", new TableType(),
//                new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), new ColIndexType(), new IntType()));
//        productions.add(new Production<>(new TableType(), "mutate", new TableType(), new ColIndexType(),
//                new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), new ColIndexType()));

        //FunctionType
//        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new IntType()), "l(a,b).(/ a b)"));
//
//        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(> a b)"));
//        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(< a b)"));
//        productions.add(new Production<>(new FunctionType(new PairType(new IntType(), new IntType()), new BoolType()), "l(a,b).(== a b)"));

//        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(> a b)", new IntType()));
//        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(< a b)", new IntType()));
//        productions.add(new Production<>(new FunctionType(new IntType(), new BoolType()), "l(a).(== a b)", new IntType()));

        // Aggregator Type
//        productions.add(new Production<>(new AggrType(), "mean"));
//        productions.add(new Production<>(new AggrType(), "min"));
//        productions.add(new Production<>(new AggrType(), "sum"));

//        // FIXME: IntType
//        productions.add(new Production<>(new IntType(), "0"));
//        productions.add(new Production<>(new IntType(), "1"));
//
//        // FIXME: ColIndexType
//        productions.add(new Production<>(new ColIndexType(), "0"));
//        productions.add(new Production<>(new ColIndexType(), "1"));
//
//        // FIXME: ListType
//        productions.add(new Production<>(new ListType(new IntType()), getNeoList()));

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
        return inputProductions;
    }


    @Override
    public List<Production<AbstractType>> getLineProductions(int size) {
        List<Production<AbstractType>> productions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            productions.add(new Production<>(new TableType(), "line" + i + "table"));
        }

        return productions;
    }

    public List<Production<AbstractType>> getInitProductions() {
        return initProductions;
    }
}
