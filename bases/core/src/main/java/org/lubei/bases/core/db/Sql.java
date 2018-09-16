package org.lubei.bases.core.db;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import groovy.lang.Closure;
import groovy.sql.GroovyRowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * 扩展groovy.sql.Sql提供分页查询等扩展功能
 */
public class Sql extends groovy.sql.Sql {

    public void setDb(Db db) {
        this.db = db;
    }

    private Db db;

    private final Logger logger = LoggerFactory.getLogger(Sql.class);

    public Sql(DataSource dataSource) {
        super(dataSource);
    }

    public static GroovyRowResult toRowResult(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        Map<String, Object> lhm = new LinkedHashMap<String, Object>(metadata.getColumnCount(), 1);
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            String label = metadata.getColumnLabel(i);
            if (label!=null && !label.equalsIgnoreCase("its_rnum")) {
                lhm.put(label, rs.getObject(i));
            }
        }
        return new GroovyRowResult(lhm);
    }

    private boolean moveCursor(ResultSet results, int offset) throws SQLException {
        if (offset == 0) {
            return true;
        }
        boolean cursorAtRow = true;
        if (results.getType() == ResultSet.TYPE_FORWARD_ONLY) {
            int i = 1;
            while (i++ < offset && cursorAtRow) {
                cursorAtRow = results.next();
            }
        } else if (offset > 1) {
            cursorAtRow = results.absolute(offset - 1);
        }
        return cursorAtRow;
    }

    /**
     * 重载原方法，以实现label过滤掉分页查询生成的字段its_rnum
     */
    @Override
    protected List<GroovyRowResult> asList(String sql, ResultSet rs, int offset, int maxRows,
                                           @SuppressWarnings("rawtypes") Closure metaClosure) throws SQLException {
        if (offset != 0) {
            return super.asList(sql, rs, offset, maxRows, metaClosure);
        }
        List<GroovyRowResult> results = new ArrayList<GroovyRowResult>();

        try {
            if (metaClosure != null) {
                metaClosure.call(rs.getMetaData());
            }

            boolean cursorAtRow = moveCursor(rs, offset);
            if (!cursorAtRow) {
                return null;
            }

            int i = 0;
            while ((maxRows <= 0 || i++ < maxRows) && rs.next()) {
                results.add(toRowResult(rs));
            }
            return (results);
        } catch (SQLException e) {
            LOG.warning("Failed to retrieve row from ResultSet for: " + sql + " because: " + e
                    .getMessage());
            throw e;
        } finally {
            rs.close();
        }
    }

    static String SQL_COUNT_TEMPLATE = "SELECT COUNT(1) FROM (\n%s\n) its_t";

    static String SQL_LIMIT_ORACLE = "SELECT * FROM (SELECT its_inner.*, ROWNUM as its_rnum FROM ("
                                     + "\n%s\n"
                                     + ") its_inner WHERE ROWNUM <= ? ) its_outer WHERE its_rnum > ?";

    /**
     * 分页查询
     *
     * @param sql       数据库查询语句
     * @param params    查询参数的列表，允许为null
     * @param pageIndex 页码，起始页码为0
     * @param pageSize  每页记录数量
     * @return 分页查询结果，包含total（总记录数）、data（分页结果——sql.rows）
     * @throws SQLException 数据库查询异常
     */
    @SuppressWarnings("rawtypes")
    public Map page(String sql, List<Object> params, int pageIndex, int pageSize)
            throws SQLException {
        logger.trace("page params:{}, sql:{}", params, sql);
        boolean hasParams = params != null && !params.isEmpty();
        String countSql = String.format(SQL_COUNT_TEMPLATE, sql);
        logger.trace("countSql:{}", countSql);
        GroovyRowResult countRow;
        if (hasParams) {
            countRow = this.firstRow(countSql, params);
        } else {
            countRow = this.firstRow(countSql);
        }
        long total = Long.parseLong(countRow.getAt(0).toString());
        List<Object> pageParams = hasParams ? Lists.newArrayList(params) : Lists.newArrayList();
        DbType dbType = db == null ? null : db.getDbType();
        if (dbType != null && db.getDbType() == DbType.Oracle) {
            pageParams.add((pageIndex + 1) * pageSize);
            pageParams.add(pageIndex * pageSize);
            sql = String.format(SQL_LIMIT_ORACLE, sql);
        } else {
            pageParams.add(pageIndex * pageSize);
            pageParams.add(pageSize);
            sql += " limit ?,?";
        }
        logger.trace("pageSql:{}", sql);
        List<GroovyRowResult> rows = this.rows(sql, pageParams);
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("total", total);
        ret.put("data", rows);
        return ret;
    }

    private static int getInteger(Map<String, String> params, String key, String name) {
        String sValue = params.get(key);
        Preconditions.checkArgument(sValue != null, "没有传入%s", name);
        Integer value = Ints.tryParse(sValue);
        Preconditions.checkArgument(value != null && value >= 0, "传入%s值[%s]非法", name, value);
        return value;
    }

    /**
     * 分页查询（专门为miniUI进行优化）
     *
     * @param sql        数据库查询语句
     * @param params     查询参数的列表，允许为null
     * @param pageParams 分页参数，一般为前台页面调用参数，包含pageIndex、pageSize、sortField、sortOrder
     * @return 分页查询结果，包含total（总记录数）、data（分页结果——sql.rows）
     * @throws SQLException 数据库查询异常
     */
    @SuppressWarnings("rawtypes")
    public Map page(String sql, List<Object> params, Map<String, String> pageParams)
            throws SQLException {
        logger.trace("page params:{}, pageParams:{}, sql:{}", params, pageParams, sql);
        int pageIndex = getInteger(pageParams, "pageIndex", "页码");
        int pageSize = getInteger(pageParams, "pageSize", "单页记录数量");
        String sortField = pageParams.get("sortField");
        String sortOrder = pageParams.get("sortOrder");
        if (!Strings.isNullOrEmpty(sortField) && !Strings.isNullOrEmpty(sortOrder)) {
            if (sortField.indexOf(',') > 0) {
                String[] fields = sortField.split(",");
                String[] orders = sortOrder.split(",");
                Preconditions.checkArgument(fields.length == orders.length, "排序字段个数与顺序类型个数不同");
                StringBuilder sb = new StringBuilder(sql);
                sb.append(" ORDER BY ");
                sb.append(fields[0]).append(' ').append(orders[0]);
                for (int i = 1, len = fields.length; i < len; i++) {
                    sb.append(", ").append(fields[i]).append(' ').append(orders[i]);
                }
                sql = sb.toString();
            } else {
                sql += String.format(" ORDER BY %s %s", sortField, sortOrder);
            }
        }
        return page(sql, params, pageIndex, pageSize);
    }
}
