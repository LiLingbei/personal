package org.lubei.bases.core.collect;

import com.google.common.base.MoreObjects;

/**
 * 简单表格
 */
public class SimpleTable extends Table<String, Object> {

    public static final String[] EMPTY_HEADER = new String[]{};
    private int updateCount = -1;
    private int time;
    private String cmd;

    public SimpleTable(String... labels) {
        super(labels);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(SimpleTable.class)
                .omitNullValues()
                .add("time", time).add("cmd", cmd).add("lables", this.labels);
        if (this.rows != null) {
            helper.add("count", this.rows.size());
            int count = this.rows.size();
            if (count > 10) {
                count = 10;
            }
            for (int i = 0; i < count; i++) {
                helper.add("row", this.rows.get(i));
            }
        }
        return helper.toString();
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
