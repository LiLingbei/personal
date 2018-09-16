package org.lubei.bases.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGPoolingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

/**
 * 数据库初始化工具
 *
 * @author liwenheng@ruijie.com.cn
 */
public class DbPatcher {

    public static final String PLACEHOLDER_PREFIX = "$$${";
    static final Logger LOGGER = LoggerFactory.getLogger(DbPatcher.class);
    private static final String DB_CONFIG_JSON = "db/config.json";

    Set<Config> configList = Sets.newLinkedHashSet();
    Flyway flyway;
    DataSource dataSource;

    /**
     * 构造函数
     *
     * @param dataSource 数据源
     * @throws IOException 配置文件读取异常
     */
    public DbPatcher(DataSource dataSource) throws IOException {
        ClassLoader classLoader = DbPatcher.class.getClassLoader();
        Enumeration<URL> resources = classLoader.getResources(DB_CONFIG_JSON);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            LOGGER.info("load config {}", url);
            String json = Resources.toString(url, Charsets.UTF_8);
            configList.addAll(JSON.parseArray(json, Config.class));
        }
        LOGGER.info("config {}", configList);

        flyway = new Flyway();
        this.dataSource = dataSource;
        flyway.setPlaceholderPrefix(PLACEHOLDER_PREFIX);
    }

    private static String[] splitToArray(String schemas) {
        return Iterables.toArray(Splitter.on(',').split(schemas), String.class);
    }

    public static void main(String[] args) throws IOException {
        String url = args.length > 0 ? args[0] : "jdbc:postgresql://127.0.0.1:5432/itone";
        String user = args.length > 1 ? args[1] : "postgres";
        String pass = args.length > 2 ? args[2] : "";
        PGPoolingDataSource pgPoolingDataSource = new PGPoolingDataSource();
        pgPoolingDataSource.setUrl(url);
        pgPoolingDataSource.setUser(user);
        pgPoolingDataSource.setPassword(pass);
        new DbPatcher(pgPoolingDataSource).migrate();
    }

    /**
     * 执行包装器
     *
     * <p>由于flyway当前版本的bug，对postgres数据库，未将searchPath还原，所以当前申请新连接池，避免影响</p>
     *
     * @param runnable 执行内容
     */
    private void runInWrapper(Runnable runnable) {
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druid = (DruidDataSource) dataSource;
            // 在druid初始化后，dataType才有值
            try {
                druid.init();
            } catch (SQLException e) {
                Throwables.propagate(e);
            }
            boolean isPostgres = Objects.equals(druid.getDbType(), JdbcConstants.POSTGRESQL);
            if (isPostgres) {   // 在flyway4.0版本修复bug之前，在新的连接池内运行
                runInNewDataSource(runnable, druid);
                return;
            }
        }
        flyway.setDataSource(dataSource);
        runnable.run();
    }

    private void runInNewDataSource(Runnable runnable, DruidDataSource druidDataSource) {
        LOGGER.info("申请临时连接池");
        PGPoolingDataSource pgPoolingDataSource = new PGPoolingDataSource();
        pgPoolingDataSource.setDataSourceName("temp");
        pgPoolingDataSource.setUrl(druidDataSource.getUrl());
        pgPoolingDataSource.setUser(druidDataSource.getUsername());
        pgPoolingDataSource.setPassword(druidDataSource.getPassword());
        try {
            flyway.setDataSource(pgPoolingDataSource);
            runnable.run();
        } finally {
            LOGGER.info("关闭临时连接池");
            pgPoolingDataSource.close();
        }
    }

    public void clean() {
        runInWrapper(this::doClean);
    }

    /**
     * 数据库清理
     */
    private void doClean() {
        List<String> allSchemas = Lists.newArrayList();
        for (Config config : configList) {
            for (String schema : Splitter.on(',').split(config.schemas)) {
                if (!allSchemas.contains(schema)) {
                    allSchemas.add(schema);
                }
            }
        }
        // 清理时的顺序与创建时相反
        String[] schemas = Iterables.toArray(Lists.reverse(allSchemas), String.class);
        flyway.setSchemas(schemas);   // 所要清理的数据库模式，按顺序
        flyway.clean();
    }

    /**
     * 数据库合并
     */
    public void migrate() {
        runInWrapper(this::doMigrate);
    }

    private void doMigrate() {
        for (Config config : configList) {
            flyway.setLocations(config.location); // 数据库脚本路径
            String[] schemas = splitToArray(config.schemas);
            flyway.setValidateOnMigrate(false);
            flyway.setSchemas(schemas);    // 数据库模式schema
            flyway.setPlaceholders(ImmutableMap.of("schema", schemas[0]));
            flyway.migrate();
        }
    }

    /**
     * 数据库补丁执行器的配置项目
     */
    public static class Config {

        /**
         * 文件路径
         */
        String location;
        /**
         * 执行时的模式（search_path）
         */
        String schemas;

        public Config() {
        }

        @Override
        public int hashCode() {
            int result = location != null ? location.hashCode() : 0;
            result = 31 * result + (schemas != null ? schemas.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Config config = (Config) o;

            if (location != null ? !location.equals(config.location) : config.location != null) {
                return false;
            }
            return schemas != null ? schemas.equals(config.schemas) : config.schemas == null;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("location", location)
                    .add("schemas", schemas)
                    .toString();
        }

        public String getSchemas() {
            return schemas;
        }

        public void setSchemas(String schemas) {
            this.schemas = schemas;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
