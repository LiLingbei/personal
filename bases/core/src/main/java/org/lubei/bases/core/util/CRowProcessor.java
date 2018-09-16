package org.lubei.bases.core.util;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 处理C_字段的行处理器，该处理器同时将支持将字段小写化放到map中。 用法举例：
 * <pre>
 * List<User> users = runner.query(sql, new BeanListHandler<User>(User.class,
 * DbUtil.COLUMN_LABEL_CONVERTER));
 * </pre>
 */
public class CRowProcessor extends BasicRowProcessor {


    private final BeanProcessor convert;

    /**
     * The default BeanProcessor instance to use if not supplied in the constructor.
     */
    public static final BeanProcessor defaultConvert = new CBeanProcessor();

    protected CRowProcessor() {
        this.convert = defaultConvert;
    }

    protected CRowProcessor(BeanProcessor convert) {
        this.convert = defaultConvert;
    }

    @Override
    public <T> T toBean(ResultSet rs, Class<? extends T> type) throws SQLException {
        return this.convert.toBean(rs, type);
    }

    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<? extends T> type) throws SQLException {
        return this.convert.toBeanList(rs, type);
    }

    @Override
    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new LowerCaseMap<Object>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for (int i = 1; i <= cols; i++) {
            String columnName = getColumnName(rsmd, i);
            result.put(columnName, rs.getObject(i));
        }

        return result;
    }

    public static class CBeanProcessor extends BeanProcessor {

        @Override
        protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props)
                throws SQLException {
            int cols = rsmd.getColumnCount();
            int[] columnToProperty = new int[cols + 1];
            Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

            for (int col = 1; col <= cols; col++) {
                String columnName = getColumnName(rsmd, col);
                for (int i = 0; i < props.length; i++) {
                    if (columnName.equalsIgnoreCase(props[i].getName())) {
                        columnToProperty[col] = i;
                        break;
                    }
                }
            }

            return columnToProperty;
        }
    }

    private static String getColumnName(ResultSetMetaData rsmd, int col) throws SQLException {
        String columnName = rsmd.getColumnLabel(col);
        if (null == columnName || 0 == columnName.length()) {
            columnName = rsmd.getColumnName(col);
        }
        if ((columnName.startsWith("c_")) || columnName.startsWith("C_")) {
            columnName = columnName.substring(2);
            columnName = columnName.replaceAll("_", "");
        }
        return columnName;
    }
}
