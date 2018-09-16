package org.lubei.bases.core.db;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

/**
 * 数据库对象管理器
 */
public class DbManager {

    private static final Logger logger = LoggerFactory.getLogger(DbManager.class);
    private final Map<String, Db> dbs;
    /**
     * 是否启用自身监控
     */
    private static boolean isSelfMonitoring;
    public DbManager() {
        dbs = Maps.newConcurrentMap();
    }

    /**
     * 创建数据库对象（不支持batis）
     *
     * @param register   注册者
     * @param id         唯一标识，建议为"类全路径.DB对象名称"
     * @param dataSource 数据源
     * @return 数据库对象
     */
    public Db buildDb(String register, String id, DataSource dataSource) {
        return buildDb(register, id, dataSource, null);
    }

    /**
     * 创建数据库对象（支持batis）
     *
     * @param register        注册者
     * @param id              唯一标识，建议为"类全路径.DB对象名称"
     * @param dataSource      数据源
     * @param batisConfReader batis配置reader
     * @return 数据库对象
     */
    public Db buildDb(String register, String id, DataSource dataSource, Reader batisConfReader) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkArgument(!dbs.containsKey(id), "already registered {}", id);
        logger.info("buildDb {} {}", id, register);
        Db db = new Db(register, id, dataSource, batisConfReader);
        dbs.put(id, db);
        return db;
    }

    /**
     * 根据数据库key获取数据信息 since 1.3.3
     */
    public Db getDb(String key) {
        return dbs.get(key);
    }

    @Override
    public String toString() {
        ToStringHelper s = MoreObjects.toStringHelper(this);
        for (Entry<String, Db> ent : dbs.entrySet()) {
            s.add(ent.getKey(), ent.getValue());
        }
        return dbs.toString();
    }
    public static boolean isSelfMonitoring() {
        return isSelfMonitoring;
    }

    public static void setIsSelfMonitoring(boolean isMelodyMonitor) {
        isSelfMonitoring = isMelodyMonitor;
    }
}
