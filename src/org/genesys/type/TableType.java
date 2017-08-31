package org.genesys.type;

/**
 * Created by yufeng on 5/31/17.
 */
public class TableType implements AbstractType {


    public TableType() {

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableType)) {
            return false;
        }
        return this.equals(obj);
    }

    @Override
    public int hashCode() {
        return 11 * this.hashCode() + 1;
    }

    @Override
    public String toString() {
        return "Table<" + this.toString() + ">";
    }
}
