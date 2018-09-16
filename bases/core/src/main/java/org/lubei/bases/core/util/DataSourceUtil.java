package org.lubei.bases.core.util;

import static com.codahale.metrics.MetricRegistry.name;
import static org.lubei.bases.core.util.Metrics.REGISTRY;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import com.codahale.metrics.Gauge;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.sql.DataSource;

/**
 * 数据源工具类，当前提供Druid连接池创建工具类
 */
public class DataSourceUtil {

    static final Logger LOGGER = LoggerFactory.getLogger(DataSourceUtil.class);

    /**
     * 使用配置字符串创建Druid连接池
     *
     * @param druidConf JSON格式的Druid配置
     * @return Druid连接池
     * @throws Exception Druid创建异常
     */
    public static DataSource createDataSource(final String druidConf) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(druidConf));
        Map<String, ?> properties = JSON.parseObject(druidConf);
        return createDataSource(properties);
    }

    /**
     * 使用配置MAP创建Druid连接池
     *
     * @param properties Druid配置
     * @return Druid连接池
     * @throws Exception Druid创建异常
     */
    public static DataSource createDataSource(final Map<String, ?> properties) throws Exception {
        return DruidDataSourceFactory.createDataSource(properties);
    }

    /**
     * 注册数据库连接池性能指标
     *
     * @param dataSource 连接池
     * @param id         名称ID
     */
    public static void registerMetric(DataSource dataSource, String id) {
        if (!(dataSource instanceof DruidDataSource)) {
            LOGGER.warn("注册数据库连接池指标失败，不是DruidDataSource");
            return;
        }
        DruidDataSource druidDataSource = (DruidDataSource) dataSource;
        try {
            REGISTRY.register(name("db", id, "activeCount"),
                              (Gauge<Integer>) () -> druidDataSource.getActiveCount());
            REGISTRY.register(name("db", id, "connectCount"),
                              (Gauge<Long>) () -> druidDataSource.getConnectCount());
            REGISTRY.register(name("db", id, "closeCount"),
                              (Gauge<Long>) () -> druidDataSource.getCloseCount());
            REGISTRY.register(name("db", id, "poolingCount"),
                              (Gauge<Integer>) () -> druidDataSource.getPoolingCount());
            REGISTRY.register(name("db", id, "executeCount"),
                              (Gauge<Long>) () -> druidDataSource.getExecuteCount());
        } catch (Throwable t) {
            LOGGER.warn("注册数据库连接池指标失败", t);
        }
    }
}
