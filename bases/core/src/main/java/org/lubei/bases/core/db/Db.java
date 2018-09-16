package org.lubei.bases.core.db;

import org.lubei.bases.core.db.batis.AutoMapperRegistry;
import org.lubei.bases.core.db.batis.SqlSessionTemplate;
import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.util.DataSourceUtil;
import org.lubei.bases.core.util.DbUtil;
import org.lubei.bases.core.util.ResourceUtil;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.apache.commons.dbutils.DbUtils;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.sql.DataSource;

/**
 * 数据库封装，当前支持DbUtils、batis、groovy.Sql
 *
 * @author liwenheng@ruijie.com.cn
 */
public class Db {

    private static final Logger logger = LoggerFactory.getLogger(Db.class);
    private final String id;
    private final String register;
    private final Date registerTime = new Date();
    private DataSource dataSource;
    private Environment environment;
    private SqlSessionFactory factory;
    @Deprecated
    private DbType dbType;

    /**
     * 构造函数
     *
     * @param register        注册者
     * @param id              全局ID
     * @param dataSource      数据源（连接池）
     * @param batisConfReader batis配置文件reader
     */
    public Db(final String register, final String id, final DataSource dataSource,
              final Reader batisConfReader) {
        super();
        this.id = id;
        this.register = register;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        this.dataSource = dataSource;
        DataSourceUtil.registerMetric(dataSource, id);
        if (batisConfReader != null) {
            initBatis(id, batisConfReader);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("id", id)
                .add("register", register)
                .add("registerTime", registerTime)
                .add("dataSource", dataSource)
                .add("environment", environment)
                .add("factory", factory)
                .toString();
    }

    /**
     * 获得数据库类型，数据库类型在创建DB时从连接的元数据中获得
     *
     * @return 数据库类型
     */
    @Deprecated
    public DbType getDbType() {
        return dbType;
    }

    /**
     * 设置数据库类型，仅供测试时使用。正常情况下数据库类型在创建DB时从连接的元数据中获得
     *
     * @param dbType 数据库类型
     */
    @Deprecated
    public void setDbType(final DbType dbType) {
        this.dbType = dbType;
    }

    /**
     * 从文件初始化batis
     *
     * @param fileName 文件名
     */
    public void initBatis(final String fileName) {
        try {
            Reader batisReader = ResourceUtil.getStringReader(fileName);
            initBatis(id, batisReader);
        } catch (IOException e) {
            throw new BusinessException("mybatis初始化错误：" + fileName, e);
        }
    }

    /**
     * 初始化batis
     *
     * @param id              id
     * @param batisConfReader batis配置的reader
     */
    public void initBatis(final String id, final Reader batisConfReader) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(dataSource);
        Preconditions.checkNotNull(batisConfReader);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        this.environment = new Environment(id, transactionFactory, dataSource);
        this.factory = new SqlSessionFactoryBuilder().build(batisConfReader);
        factory.getConfiguration().setEnvironment(environment);
        AutoMapperRegistry.inject(this.factory);
    }

    /**
     * 得到Groovy.Sql，不需要关闭
     *
     * @return 扩展过的Sql对象（支持page分页查询）
     */
    public Sql getSql() {
        Sql sql = new Sql(dataSource);
        sql.setDb(this);
        return sql;
    }

    /**
     * 得到QueryRunner，不需要关闭
     *
     * @return 扩展过的QueryRunner（支持insert方法）
     */
    public DbRunner getRunner() {
        return new DbRunner(dataSource);
    }

    /**
     * QueryRunner事务处理
     *
     * @param transCall 事务执行
     * @param <T>       返回值类型
     * @return 执行结果
     * @throws SQLException 数据库异常
     */
    @Deprecated
    public <T> T withTransaction(final TransCall<T> transCall) throws SQLException {
        Connection conn = dataSource.getConnection();
        boolean savedAutoCommit = true;
        try {
            savedAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            transCall.transRunner = new ConnectedDbRunner(conn);
            T ret = transCall.call();
            conn.commit();
            return ret;
        } catch (Throwable e) {
            conn.rollback();
            throw new SQLException(e);
        } finally {
            conn.setAutoCommit(savedAutoCommit);
            DbUtils.closeQuietly(conn);
        }
    }

    /**
     * 得到数据源
     *
     * @return 数据源
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * batis事务处理
     *
     * 当Transaction发生异常时，受检异常将转换为RuntimeException
     *
     * @param transaction 事务执行
     * @param <T>         返回值类型
     * @return 返回值
     * @deprecated 使用{@link com.its.itone.core.db.Db#withTransaction(java.util.function.Function)}
     */
    @Deprecated
    public <T> T withTransaction(final Transaction<T> transaction) {
        Preconditions.checkNotNull(factory);
        SqlSession session = factory.openSession(false);
        try {
            transaction.transSession = session;
            T ret = transaction.call();
            session.commit();
            return ret;
        } catch (Throwable throwable) {
            session.rollback();
            throw Throwables.propagate(throwable);
        } finally {
            DbUtil.closeQuietly(session);
        }
    }

    /**
     * 执行batis事务
     *
     * @param consumer 操作执行
     */
    public void withTransaction(final Consumer<SqlSession> consumer) {
        withSession(consumer, false);
    }

    /**
     * 在一个session内执行batis操作(自动提交）
     *
     * @param consumer 操作执行
     */
    public void withSession(final Consumer<SqlSession> consumer) {
        withSession(consumer, true);
    }

    /**
     * 在一个session内执行batis操作(自动提交）
     *
     * @param consumer   操作
     * @param autoCommit 单步是否自动提交
     */
    private void withSession(final Consumer<SqlSession> consumer, boolean autoCommit) {
        Preconditions.checkNotNull(factory);
        SqlSession session = factory.openSession(autoCommit);
        try {
            consumer.accept(session);
            if (!autoCommit) {
                session.commit();
            }
        } catch (Throwable throwable) {
            if (!autoCommit) {
                session.rollback();
            }
            throw Throwables.propagate(throwable);
        } finally {
            DbUtil.closeQuietly(session);
        }
    }

    /**
     * 执行batis事务
     *
     * @param consumer 操作执行
     */
    public <T> T withTransaction(final Function<SqlSession, T> consumer) {
        return withSession(consumer, false);
    }

    /**
     * 在一个session内执行batis操作(自动提交）
     *
     * @param consumer 操作执行
     */
    public <T> T withSession(final Function<SqlSession, T> consumer) {
        return withSession(consumer, true);
    }

    /**
     * 在一个session内执行batis操作(自动提交）
     *
     * @param consumer   操作
     * @param autoCommit 单步是否自动提交
     */
    public <T> T withSession(final Function<SqlSession, T> consumer, boolean autoCommit) {
        Preconditions.checkNotNull(factory);
        SqlSession session = factory.openSession(autoCommit);
        try {
            T apply = consumer.apply(session);
            if (!autoCommit) {
                session.commit();
            }
            return apply;
        } catch (Throwable throwable) {
            if (!autoCommit) {
                session.rollback();
            }
            throw Throwables.propagate(throwable);
        } finally {
            DbUtil.closeQuietly(session);
        }
    }

    /**
     * 获得batis工厂
     *
     * @return batis工厂
     */
    public SqlSessionFactory getFactory() {
        return factory;
    }

    /**
     * 打开batis Session，需要关闭！！！
     *
     * @param autoCommit 是否自动提交
     * @return session
     * @deprecated 1.2版本将改为private
     */
    @Deprecated
    public SqlSession openSession(final boolean autoCommit) {
        return factory.openSession(autoCommit);
    }

    /**
     * 向batis增加映射包（package）
     *
     * @param packageName 包名
     */
    public void addMappers(final String packageName) {
        Preconditions.checkNotNull(factory);
        factory.getConfiguration().addMappers(packageName);
    }

    /**
     * 获取mapper.
     *
     * 该mapper的每次调用时自动打开关闭session
     *
     * @param type 类型
     * @param <T>  接口类型
     * @return 接口实例
     */
    public <T> T getMapper(Class<T> type) {
        SqlSessionTemplate session = new SqlSessionTemplate(factory);
        return session.getMapper(type);
    }

    /**
     * QueryRunner事务执行
     *
     * @param <T> 返回值类型
     */
    public static abstract class TransCall<T> implements Callable<T> {

        /**
         * 支持事务的QueryRunner
         */
        protected DbRunner transRunner;
    }

    /**
     * batis事务执行
     *
     * @param <T> 返回值类型
     */
    @Deprecated
    public static abstract class Transaction<T> implements Callable<T> {

        protected SqlSession transSession;

    }

}
