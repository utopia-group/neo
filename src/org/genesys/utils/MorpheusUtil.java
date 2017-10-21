package org.genesys.utils;

import krangl.DataCol;
import krangl.DataFrame;

import javax.xml.crypto.Data;
import java.util.*;

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

    public Set<String> getHeader(DataFrame df) {
        Set set = new HashSet();
        for (DataCol col : df.getCols()) {
            set.add(col.getName());
        }
        return set;
    }

    public Set<String> getContent(DataFrame df) {
        Set set = new HashSet();
        for (DataCol col : df.getCols()) {
            set.add(col.getName());
        }

        for (List row : df.getRawRows()) {
            for (Object o : row)
                set.add(o.toString());
        }
        return set;
    }

    //compute src - tgt
    public Set setDiff(Set src, Set tgt) {
        Set diff = new HashSet(src);
        diff.removeAll(tgt);
        return diff;
    }

    //Select a sublist from data
    public List<Integer> sel(Set<Integer> selectors, List<Integer> data) {
        assert selectors.size() > 0;
        assert data.size() > 0;
        Set<Integer> negSet = new HashSet<>();
        for (int e : selectors) {
            if (e == -99)
                negSet.add(0);
            else
                negSet.add(-e);
        }
        List<Integer> sublist = new ArrayList<>();
        boolean isNeg = selectors.iterator().next() < 0;
        for (int i = 0; i < data.size(); i++) {
            if (!isNeg) {
                if (selectors.contains(i))
                    sublist.add(i);
            } else {
                if (!negSet.contains(i))
                    sublist.add(i);
            }
        }
        return sublist;
    }

    // Given a list selector and collist, check whether all selected columns share the same type.
    public boolean hasSameType(List<Integer> sel, List<DataCol> cols) {
        int size = cols.size();
        assert size > 0;
        Set<String> colNames = new HashSet<>();
        for (int idx : sel) {
            assert idx < size : idx;
            String name = cols.get(idx).getClass().getSimpleName();
            if (!name.equals("StringCol")) name = "num";
            colNames.add(name);
        }
        return colNames.size() == 1;
    }

    public static void main(String[] args) {
        Set<Integer> sel = new HashSet<>(Arrays.asList(1, 3, 5));
        Set<Integer> sel2 = new HashSet<>(Arrays.asList(-5, -99));
        Set<Integer> sel3 = new HashSet<>(Arrays.asList(-1, -3));
        Set<Integer> sel4 = new HashSet<>(Arrays.asList(0, 2));

        List<Integer> data = Arrays.asList(0, 1, 2, 3, 4, 5);

        System.out.println(MorpheusUtil.getInstance().sel(sel, data));
        System.out.println(MorpheusUtil.getInstance().sel(sel2, data));
        System.out.println(MorpheusUtil.getInstance().sel(sel3, data));
        System.out.println(MorpheusUtil.getInstance().sel(sel4, data));
    }

}
