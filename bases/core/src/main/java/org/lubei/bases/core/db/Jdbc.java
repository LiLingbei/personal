package org.lubei.bases.core.db;

import static com.google.common.base.Preconditions.checkNotNull;

import org.lubei.bases.core.collect.SimpleTable;

import com.alibaba.druid.util.JdbcUtils;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.dbutils.DbUtils;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.sql.DataSource;

/**
 * JDBC工具类
 */
public class Jdbc implements Closeable {

    public static final String PROTOCOL_TYPE = "JDBC";
    /**
     * 协议字段：数据库URL
     */
    public static final String URL = "URL";
    /**
     * 协议字段：用户名
     */
    public static final String USER = "User";
    public static final String COUNT = "count";
    /**
     * 数据库驱动加载器
     */
    private static final LoadingCache<String, Driver> DRIVER_LOADER = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, Driver>() {
                @Override
                public Driver load(String key) throws Exception {
                    return JdbcUtils.createDriver(key);
                }
            });
    /**
     * 协议字段：密码
     */
    public static String PASS = "Password";
    private Connection connection;
    private DataSource dataSource;
    private volatile boolean pmdKnownBroken = false;

    private Jdbc() {
    }

    /**
     * 判断协议类型是否支持
     *
     * @param type 协议类型
     * @return 是否支持
     */
    public static boolean isValidType(String type) {
        return Objects.equals(PROTOCOL_TYPE, type);
    }

    /**
     * 生成执行对象，必须关闭
     *
     * @param protocolMap 协议
     * @return 执行对象（必须关闭）
     */
    public static Jdbc build(Map<String, Object> protocolMap) {
        try {
            Connection connection = buildConnection(protocolMap);
            Jdbc jdbc = new Jdbc();
            jdbc.connection = connection;
            return jdbc;
        } catch (Throwable e) {
            throw new IllegalStateException("JDBC连接失败" + e.getMessage(), e);
        }
    }

    /**
     * 生成执行对象，必须关闭
     *
     * @param dataSource 数据源
     * @return 执行对象
     */
    public static Jdbc build(DataSource dataSource) {
        Jdbc jdbc = new Jdbc();
        jdbc.dataSource = dataSource;
        return jdbc;
    }

    /**
     * 通过协议创建JDBC连接
     *
     * @param protocol 协议信息
     * @return jdbc连接
     * @throws SQLException       数据库连接失败
     * @throws ExecutionException 数据库驱动加载
     */
    private static Connection buildConnection(Map<String, Object> protocol)
            throws SQLException, ExecutionException {
        DriverManager.setLoginTimeout(10);
        String url = checkNotNull((String) protocol.get(URL), URL + "不能为空");
        String driverClassName = JdbcUtils.getDriverClassName(url);
        Driver driver = DRIVER_LOADER.get(driverClassName);
        String user = (String) protocol.get(USER);
        String pass = (String) protocol.get(PASS);
        Properties info = new Properties();
        if (!Strings.isNullOrEmpty(user)) {
            info.put("user", user);
        }
        if (!Strings.isNullOrEmpty(pass)) {
            info.put("password", pass);
        }
        return driver.connect(url, info);
    }

    /**
     * 释放连接
     *
     * @throws IOException 不可能发生异常
     */
    @Override
    public void close() throws IOException {
        DbUtils.closeQuietly(connection);
    }

    /**
     * 查询sql
     *
     * @param sql sql语句
     * @return 查询结果List of map
     * @throws SQLException 数据库操作异常
     */
    public SimpleTable table(String sql) throws SQLException {
        return table(sql, (Object[]) null);
    }

    /**
     * 查询sql
     *
     * @param sql    sql语句
     * @param params sql语句参数
     * @return 查询结果List of map。如果不返回记录（如更新等）返回更新记录数量。
     * @throws SQLException 数据库操作异常
     */
    public SimpleTable table(String sql, Object... params) throws SQLException {
        long start = System.currentTimeMillis();
        if (connection == null) {
            connection = dataSource.getConnection();
        }
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            if (params != null && params.length > 0) {
                fillStatement(statement, params);
            }
            SimpleTable table = getTable(statement);
            table.setCmd(sql);
            table.setTime((int) (System.currentTimeMillis() - start));
            return table;
        } finally {
            DbUtils.closeQuietly(statement);
        }
    }

    private SimpleTable getTable(PreparedStatement statement) throws SQLException {
        boolean hasResult = statement.execute();
        if (hasResult) {
            ResultSet resultSet = statement.getResultSet();
            try {
                return SimpleTableHandler.INSTANCE.handle(resultSet);
            } finally {
                DbUtils.closeQuietly(resultSet);
            }
        } else {
            int updateCount = statement.getUpdateCount();
            SimpleTable table = new SimpleTable(COUNT);
            table.addRow(updateCount);
            table.setUpdateCount(updateCount);
            return table;
        }
    }

    public void fillStatement(PreparedStatement stmt, Object... params)
            throws SQLException {

        // check the parameter count, if we can
        ParameterMetaData pmd = null;
        if (!pmdKnownBroken) {
            pmd = stmt.getParameterMetaData();
            int stmtCount = pmd.getParameterCount();
            int paramsCount = params == null ? 0 : params.length;

            if (stmtCount != paramsCount) {
                throw new SQLException("Wrong number of parameters: expected "
                                       + stmtCount + ", was given " + paramsCount);
            }
        }

        // nothing to do here
        if (params == null) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                stmt.setObject(i + 1, params[i]);
            } else {
                // VARCHAR works with many drivers regardless
                // of the actual column type. Oddly, NULL and
                // OTHER don't work with Oracle's drivers.
                int sqlType = Types.VARCHAR;
                if (!pmdKnownBroken) {
                    try {
                        /*
                         * It's not possible for pmdKnownBroken to change from
                         * true to false, (once true, always true) so pmd cannot
                         * be null here.
                         */
                        sqlType = pmd.getParameterType(i + 1);
                    } catch (SQLException e) {
                        pmdKnownBroken = true;
                    }
                }
                stmt.setNull(i + 1, sqlType);
            }
        }
    }
}
