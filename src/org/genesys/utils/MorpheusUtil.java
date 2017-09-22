package org.genesys.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yufeng on 9/5/17.
 */
public class MorpheusUtil {

    private static MorpheusUtil instance = null;

    private String prefix_ = "MORPHEUS";

    private int counter_ = 0;

    public static MorpheusUtil getInstance() {
        if (instance == null) {
            instance = new MorpheusUtil();
        }
        return instance;
    }

    public String getMorpheusString() {
        counter_++;
        return prefix_ + counter_;
    }

    private void getSubsets(List<Integer> superSet, int k, int idx, Set<Integer> current, List<Set<Integer>> solution) {
        //successful stop clause
        if (current.size() == k) {
            solution.add(new HashSet<>(current));
            return;
        }
        //unsuccessful stop clause
        if (idx == superSet.size()) return;
        Integer x = superSet.get(idx);
        current.add(x);
        //"guess" x is in the subset
        getSubsets(superSet, k, idx + 1, current, solution);
        current.remove(x);
        //"guess" x is not in the subset
        getSubsets(superSet, k, idx + 1, current, solution);
    }

    public List<Set<Integer>> getSubsets(List<Integer> superSet, int k) {
        List<Set<Integer>> res = new ArrayList<>();
        getSubsets(superSet, k, 0, new HashSet<Integer>(), res);
        return res;
    }

    public Set<Integer> negateSet(Set<Integer> orgSet) {
        Set<Integer> tgtSet = new HashSet<>();
        for (Integer i : orgSet) {
            if (i == 0) //hack for -0
                tgtSet.add(-99);
            else
                tgtSet.add(i * (-1));
        }
        return tgtSet;
    }

    public void reset() {
        counter_ = 0;
    }
}
