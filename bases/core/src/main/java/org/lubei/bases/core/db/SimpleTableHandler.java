package org.lubei.bases.core.db;

import org.lubei.bases.core.collect.SimpleTable;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Table查询处理器
 */
public class SimpleTableHandler implements ResultSetHandler<SimpleTable> {

    public static final SimpleTableHandler INSTANCE = new SimpleTableHandler();

    private SimpleTableHandler() {

    }

    @Override
    public SimpleTable handle(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] labels = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            String label = metaData.getColumnLabel(i);
            labels[i - 1] = label.toLowerCase();
        }
        SimpleTable table = new SimpleTable(labels);
        table.setUpdateCount(-1);
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            table.addRow(row);
        }
        return table;
    }
}
