package org.lubei.bases.core.db;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 持有数据库连接QueryRunner，仅供Db中事务处理使用！！！
 */
class ConnectedDbRunner extends DbRunner {

    private Connection connection;

    public ConnectedDbRunner() {
        throw new IllegalAccessError("not implement");
    }

    public ConnectedDbRunner(final boolean pmdKnownBroken) {
        throw new IllegalAccessError("not implement");
    }

    public ConnectedDbRunner(final Connection connection) {
        super();
        this.connection = connection;
    }

    public ConnectedDbRunner(final Connection connection, final boolean pmdKnownBroken) {
        super(pmdKnownBroken);
        this.connection = connection;
    }

    @Override
    public int[] batch(final String sql, final Object[][] params) throws SQLException {
        return super.batch(connection, sql, params);
    }

    @Override
    public <T> T query(final String sql, final ResultSetHandler<T> rsh, final Object... params)
            throws SQLException {
        return super.query(connection, sql, rsh, params);
    }

    @Override
    public <T> T query(final String sql, final ResultSetHandler<T> rsh) throws SQLException {
        return super.query(connection, sql, rsh);
    }

    @Override
    public int update(final String sql) throws SQLException {
        return super.update(connection, sql);
    }

    @Override
    public int update(final String sql, final Object param) throws SQLException {
        return super.update(connection, sql, param);
    }

    @Override
    public int update(final String sql, final Object... params) throws SQLException {
        return super.update(connection, sql, params);
    }

    @Override
    public <T> T insert(final String sql) throws SQLException {
        return super.insert(connection, sql);
    }

    @Override
    public <T> T insert(final String sql, final Object param) throws SQLException {
        return super.insert(connection, sql, param);
    }

    @Override
    public <T> T insert(final String sql, final Object... params) throws SQLException {
        return super.insert(connection, sql, params);
    }

}
