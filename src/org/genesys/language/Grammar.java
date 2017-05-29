package org.genesys.language;


import java.util.List;

/**
 * Created by yufeng on 5/26/17.
 */
public interface Grammar<T> {

    T start();

    String getName();

    List<Production<T>> getProductions();

    List<Production<T>> productionsFor(T symbol);

}
