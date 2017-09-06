package org.genesys.utils;

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

    public void reset() {
        counter_ = 0;
    }
}
