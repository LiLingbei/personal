package org.lubei.bases.core;

import org.lubei.bases.core.cache.CacheManager;
import org.lubei.bases.core.db.Db;
import org.lubei.bases.core.db.DbManager;
import org.lubei.bases.core.task.TaskManager;
import org.lubei.bases.core.util.DataSourceUtil;
import org.lubei.bases.core.util.ResourceUtil;
import org.lubei.bases.core.util.ServerUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;

/**
 * 全局资源管理器.
 * <p>
 * <p>提供全局资源的管理，如：数据库缓存等</p>
 */
public class GlobalRes {

    static final Logger logger = LoggerFactory.getLogger(GlobalRes.class);

    //  数据库配置文件
    private static final String ITS_DB_CONFIG_JSON = "its_db_config.json";
    private static final Server server;
    private static TaskManager serviceManager;
    private static CacheManager cacheManager;
    private static DbManager dbManager;
    private static Db defaultDd;
    private static Map<String, BlockingQueue> blockingQueueMap;

    static {
        server = ServerUtil.INSTANCE.getServer();
        blockingQueueMap = new ConcurrentHashMap<>();
        try {
            assureDbManager();
            String json = ResourceUtil.getString(ITS_DB_CONFIG_JSON);
            JSONObject jsonObject = JSON.parseObject(json);
            if (jsonObject.size() > 0) {
                for (String dbKey : jsonObject.keySet()) {
                    DataSource dataSource =
                            DataSourceUtil.createDataSource(jsonObject.getString(dbKey));
                    logger.warn("registerDb:    " + dbKey + "  " + jsonObject.getString(dbKey));
                    buildDb(dbKey, dataSource);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("its_db_config.json初始化异常，如果不需要数据库配置可以忽略此异常。{}", e.getMessage());
        } catch (Exception e) {
            logger.error("its_db_config.json初始化异常{}", e.getMessage(), e);
        }

    }

    private GlobalRes() {
        throw new IllegalAccessError("不能实例化");
    }


    public static Server getServer() {
        return server;
    }

    /**
     * 服务器ID。
     * 进程运行后自动生成，生成后自动记录到配置文件，后续保持不变
     * 不允许修改
     */
    public static String getServerId() {
        return server.getId();
    }

    /**
     * 服务名称。
     * 进程运行后自动生成，生成后自动记录到配置文件，后续保持不变
     * 名称后续可以提供修改接口，并保存
     */
    public static String getServerName() {
        return server.getName();
    }

    public static Db getDefaultDd() {
        return defaultDd;
    }

    /**
     * @param defaultDd - defaultDd{parameter description}.
     */
    public static final void setDefaultDd(final Db defaultDd) {
        GlobalRes.defaultDd = defaultDd;
    }

    private static synchronized TaskManager assureServiceManager() {
        if (serviceManager == null) {
            serviceManager = new TaskManager();
        }
        return serviceManager;
    }

    private static synchronized CacheManager assureCacheManager() {
        if (cacheManager == null) {
            cacheManager = new CacheManager();
        }
        return cacheManager;
    }

    private static synchronized DbManager assureDbManager() {
        if (dbManager == null) {
            dbManager = new DbManager();
        }
        return dbManager;
    }

    /**
     * 注册并启动服务
     *
     * @param id        服务ID
     * @param clazzName 类名称（该类必须为guava.Service）
     * @return 启动后的服务
     */
    public static synchronized Service startService(final String id, final String clazzName) {
        String register = getLoaderName();
        return assureServiceManager().startService(register, id, clazzName);
    }

    /**
     * 批量启动服务.
     *
     * @param confContent 配置信息（JSON字符串，ServiceEntry的列表）
     */
    public static synchronized void startServices(final String confContent) {
        String register = getLoaderName();
        assureServiceManager().startServices(register, confContent);
    }

    /**
     * 注册数据库（无batis).
     *
     * @param id         唯一标识
     * @param dataSource 数据源
     * @return 数据库对象
     */
    public static Db buildDb(final String id, final DataSource dataSource) {
        String register = getLoaderName();
        return assureDbManager().buildDb(register, id, dataSource);
    }

    /**
     * 注册数据库（有batis).
     *
     * @param id              唯一标识
     * @param dataSource      数据源
     * @param batisConfReader batis配置reader
     * @return 数据库对象
     */
    public static Db buildDb(final String id, final DataSource dataSource,
                             final Reader batisConfReader) {
        String register = getLoaderName();
        return assureDbManager().buildDb(register, id, dataSource, batisConfReader);
    }

    /**
     * 注册缓存。注：首个缓存注册时将其的缓存清理器.
     *
     * @param id    唯一标识
     * @param cache 缓存
     * @return 缓存
     */
    @SuppressWarnings("rawtypes")
    public static Cache registerCache(final String id, final Cache cache) {
        String register = getLoaderName();
        return assureCacheManager().registerCache(register, id, cache);
    }

    @SuppressWarnings("rawtypes")
    public static Cache getCache(final String id) {
        return assureCacheManager().getCache(id);
    }

    /**
     * 通过stackTrace获得调用该方法的上级调用方法.
     *
     * @return 调用者的类和方法名
     */
    private static String getLoaderName() {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        StackTraceElement ste = st[3];
        String className = ste.getClassName();
        String methodName = ste.getMethodName();
        return className + "." + methodName;
    }

    /**
     * 获取各资源状态的字符串.
     *
     * @return 各资源状态字符串
     */
    public static String getStat() {
        return MoreObjects.toStringHelper(GlobalRes.class)
                .add("serviceManager", serviceManager)
                .add("cacheManager", cacheManager)
                .add("dbManager", dbManager)
                .toString();
    }

    /**
     * 获取数据库.<br> 多数据库支持，根据key获取数据库
     *
     * @param key 数据库标识
     * @return Db
     */
    public static Db getDb(final String key) {
        return dbManager.getDb(key);
    }

    /**
     * 获取数据库.<br> 多数据库支持，根据key获取数据库
     *
     * @param mybatisFile mybatis配置文件名，如its_mybatis_config.xml
     * @return String
     */
    public static Db getDb(final String key, final String mybatisFile) {
        Db db = dbManager.getDb(key);
        db.initBatis(mybatisFile);
        return db;
    }


    /**
     * 获取指定队列,如果没有则创建
     *
     * @param key :队列key值
     * @return :队列
     */
    public synchronized static BlockingQueue getQueue(String key) {
        if (!blockingQueueMap.containsKey(key)) {
            blockingQueueMap.put(key, new LinkedBlockingQueue());
        }
        return blockingQueueMap.get(key);
    }


}
