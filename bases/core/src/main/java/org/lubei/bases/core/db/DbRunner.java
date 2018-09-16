package org.lubei.bases.core.db;

import org.apache.commons.dbutils.QueryRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * 扩展QueryRunner，提供insert后获取数据库生成的字段（自增ID等）的功能
 */
public class DbRunner extends QueryRunner {

    public DbRunner() {
        super();
    }

    public DbRunner(boolean pmdKnownBroken) {
        super(pmdKnownBroken);
    }

    public DbRunner(DataSource ds, boolean pmdKnownBroken) {
        super(ds, pmdKnownBroken);
    }

    public DbRunner(DataSource ds) {
        super(ds);
    }

    public <T> T insert(Connection conn, String sql) throws SQLException {
        return this.insert(conn, false, sql, (Object[]) null);
    }

    public <T> T insert(Connection conn, String sql, Object param) throws SQLException {
        return this.insert(conn, false, sql, new Object[]{param});
    }

    public <T> T insert(Connection conn, String sql, Object... params) throws SQLException {
        return insert(conn, false, sql, params);
    }

    /**
     * 插入数据
     *
     * @param sql 数据库操作语句
     * @param <T> 自增字段的类型
     * @return 自增字段值
     * @throws SQLException 数据库异常
     */
    public <T> T insert(String sql) throws SQLException {
        Connection conn = this.prepareConnection();
        return this.insert(conn, true, sql, (Object[]) null);
    }

    /**
     * 插入数据
     *
     * @param sql   数据库操作语句
     * @param param 操作参数
     * @param <T>   自增字段的类型
     * @return 自增字段值
     * @throws SQLException 数据库异常
     */
    public <T> T insert(String sql, Object param) throws SQLException {
        Connection conn = this.prepareConnection();
        return this.insert(conn, true, sql, new Object[]{param});
    }

    /**
     * 插入数据
     *
     * @param sql    数据库操作语句
     * @param params 操作参数
     * @param <T>    自增字段的类型
     * @return 自增字段值
     * @throws SQLException 数据库异常
     */
    public <T> T insert(String sql, Object... params) throws SQLException {
        Connection conn = this.prepareConnection();
        return this.insert(conn, true, sql, params);
    }

    @SuppressWarnings("unchecked")
    private <T> T insert(Connection conn, boolean closeConn, String sql, Object... params)
            throws SQLException {
        if (conn == null) {
            throw new SQLException("Null connection");
        }

        if (sql == null) {
            if (closeConn) {
                close(conn);
            }
            throw new SQLException("Null SQL statement");
        }

        PreparedStatement stmt = null;
        Object key = null;

        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            this.fillStatement(stmt, params);
            stmt.executeUpdate();
            ResultSet resultSet = stmt.getGeneratedKeys();
            if (resultSet.next()) {
                key = resultSet.getObject(1);
            }
        } catch (SQLException e) {
            this.rethrow(e, sql, params);
        } finally {
            close(stmt);
            if (closeConn) {
                close(conn);
            }
        }

        return (T) key;
    }
}
