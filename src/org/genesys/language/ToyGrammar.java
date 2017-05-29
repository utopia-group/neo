package org.genesys.language;

import org.genesys.utils.MultivalueMap;

import java.util.List;

/**
 * Created by yufeng on 5/26/17.
 */
public class ToyGrammar<T> implements Grammar<T> {

    private final T start;

    private String name;

    private List<Production<T>> productions;

    private MultivalueMap<T, Production<T>> productionsBySymbol;

    public ToyGrammar(T start, MultivalueMap<T, Production<T>> prods) {
        this.start = start;
        this.productionsBySymbol = prods;
    }

    public void init() {
        productionsBySymbol = new MultivalueMap<>();

        for (Production prod : productions) {
            String func = prod.function;
            String src = prod.source;
            productionsBySymbol.add((T) src, prod);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Production<T>> getProductions() {
        return productions;
    }

    @Override
    public T start() {
        return this.start;
    }

    @Override
    public List<Production<T>> productionsFor(T symbol) {
        System.out.println(this.productionsBySymbol + " " + symbol);
        return this.productionsBySymbol.get(symbol);
    }

}
