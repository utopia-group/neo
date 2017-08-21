package org.genesys;

/**
 * Created by yufeng on 8/20/17.
 */
public class Test {

    public static void main(String[] args) {
        String str = "(declare-const IN0_MAX_SPEC Int)(declare-const IN1_MAX_SPEC Int)(declare-const OUT_MAX_SPEC Int)(assert (<= IN0_MAX_SPEC OUT_MAX_SPEC))";
        String postfix = "_23";
        System.out.println(str);
        str = str.replaceAll("IN[0-9]_MAX_SPEC", "$0" + postfix);
        System.out.println(str);
    }
}
