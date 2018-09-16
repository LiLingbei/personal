package org.lubei.bases.common;

import com.google.common.base.Preconditions;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Uninterruptibles;
import groovy.lang.Binding;
import jodd.util.StringUtil;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.lubei.bases.common.plugin.PluginDaemon;
import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.db.Db;
import org.lubei.bases.core.db.DbManager;
import org.lubei.bases.core.rpc.RpcConfig;
import org.lubei.bases.core.service.IBaseService;
import org.lubei.bases.core.service.IService;
import org.lubei.bases.core.service.Services;
import org.lubei.bases.core.service.ServicesFactory;
import org.lubei.bases.core.util.GroovyUtil;
import org.lubei.bases.core.util.Metrics;
import org.lubei.bases.core.util.ResourceUtil;
import org.lubei.bases.core.util.RpcDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

public final class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String ITS_BATIS_RIIL_XML = "its_mybatis_config.xml";
    private static final String ITONE_DATA_SOURCE = "itone.dataSource";
    private static final String ITS_CONFIG_FILE = "its_config.json";
    public static Db db;
    /**
     * 是否单机版
     */
    private static boolean isSingle;
    private static ItsConfigBean appConfig;
    private static DaemonManager appDaemonManager;

    static {
        loadConfig();
        configSelfMonitoring();
        db = buildDb(ITONE_DATA_SOURCE, ITS_BATIS_RIIL_XML);
    }

    private App() {
        throw new IllegalAccessError("不能实例化");
    }

    private static void configSelfMonitoring() {
        if (!appConfig.isSelfMonitoring()) {
            return;
        }
        String configFile = Resources.getResource(ITS_CONFIG_FILE).getPath();
        DbManager.setIsSelfMonitoring(appConfig.isSelfMonitoring());
//        RpcMonitor.INSTANCE().startAsync();
    }

    /**
     * 获取全局配置参数
     *
     * @return 配置参数
     */
    public static ItsConfigBean getAppConfig() {
        return appConfig;
    }

    /**
     * 读取配置文件，并将全集配置合并到其中
     *
     * @param resFilename 配置文件名称
     * @param clazz       配置文件类型
     * @param <T>         ItsConfigBean子类
     * @return 配置对象
     */
    public static <T extends ItsConfigBean> T readAppConfig(String resFilename, Class<T> clazz) {
        T subConfig = ResourceUtil.readJson(resFilename, clazz);
        appConfig.copyTo(subConfig);
        return subConfig;
    }

    /**
     * 从配置文件加载参数
     */
    private static void loadConfig() {
        LOGGER.debug("从配置文件解析配置参数，文件名为 {}", ITS_CONFIG_FILE);
        appConfig = ResourceUtil.readJson(ITS_CONFIG_FILE, ItsConfigBean.class);
        LOGGER.info("全局配置为：{}", appConfig);
        isSingle = appConfig.isSingle();
        RpcConfig.INSTANCE.setRpcUrl(appConfig.getRpc());
        if (!Strings.isNullOrEmpty(appConfig.getRpcServerClass())) {
            RpcConfig.INSTANCE.setRpcServerClass(appConfig.getRpcServerClass());
        }
        if (!Strings.isNullOrEmpty(appConfig.getRpcClientClass())) {
            RpcConfig.INSTANCE.setRpcClientClass(appConfig.getRpcClientClass());
        }
    }

    public static void main(String[] args) {
        printInfo();
        initMetrics();

        try {
            startDaemons();
        } catch (Throwable throwable) {
            LOGGER.error("启动失败：", throwable);
        }
        bootGroovy();
        for (; ; ) {
            LOGGER.debug("running");
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.MINUTES);
        }
    }

    /**
     * 特殊启动加载
     *
     * 如：influxReporter等。由于其参数可能随现场改变，且不是必须的，所以改为groovy定制
     */
    public static void bootGroovy() {
        try {
            Class loadClass = GroovyUtil.loadClass("boot.groovy");
            InvokerHelper.createScript(loadClass, new Binding()).run();
        } catch (Throwable t) {
            LOGGER.warn("特殊启动加载 fail", t);
        }
    }

    public static void initMetrics() {
        Metrics.initBasicMetrics();
    }

    public static void printInfo() {
        ImmutableList<StandardSystemProperty> systemProperties =
                ImmutableList.of(StandardSystemProperty.JAVA_VERSION,
                                 StandardSystemProperty.JAVA_IO_TMPDIR,
                                 StandardSystemProperty.USER_NAME,
                                 StandardSystemProperty.OS_ARCH,
                                 StandardSystemProperty.OS_NAME,
                                 StandardSystemProperty.OS_VERSION);
        String path = new File(".").getAbsolutePath();
        LOGGER.info("开始启动： 当前路径 {}, 环境变量, {}", path, systemProperties);
        ClassLoader classLoader = App.class.getClassLoader();
        Map<String, Long> classPaths = Maps.newLinkedHashMap();
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            for (URL url : urls) {
                String filePath = url.getFile();
                File file = new File(filePath);
                String relativePath = StringUtil.remove(file.getAbsolutePath(), path);
                classPaths.put(relativePath, file.length());
            }
        }
        LOGGER.info("classPath {}", classPaths);
    }


    /**
     * 启动服务
     */
    protected static void startDaemons() {
        List<String> daemons = Preconditions.checkNotNull(appConfig).getDaemons();
        Preconditions.checkNotNull(daemons);

        List<Service> services = Lists.newArrayList();
        loadRpcDaemon(services);
        //追加插件Daemon
        appendPluginDaemon(daemons);
        //
        for (String daemonClazz : daemons) {
            try {
                Service service = (Service) Class.forName(daemonClazz).newInstance();
                services.add(service);
            } catch (Throwable t) {
                LOGGER.error("加载服务类失败:{}", daemonClazz);
                Throwables.propagate(t);
            }
        }
        LOGGER.info("加载并初始化服务类结束，开始启动: {}", services);
        DaemonManager manager = new DaemonManager(services);
        appDaemonManager = manager;
        appDaemonManager.start();
    }

    /**
     * 追加插件Daemon类配置
     */
    private static void appendPluginDaemon(List<String> daemons) {
        int index = daemons.indexOf(DbPatcherDaemon.class.getName());
        // 如果不存在DbPatcher,追加到第一个，否则追加到DbPatcher后。
        if (index < 0) {
            daemons.add(0, PluginDaemon.class.getName());
        } else {
            daemons.add(index + 1, PluginDaemon.class.getName());
        }

    }

    private static void loadRpcDaemon(List<Service> services) {
        if (!appConfig.isSingle()) { // 分布式部署情况下启动rpc服务
            String rpc = appConfig.getRpc();
            if (!Strings.isNullOrEmpty(rpc)) {
                services.add(new RpcDaemon(RpcConfig.INSTANCE));
            } else {
                LOGGER.warn("不是单一部署模式，但是rpc未设置，rpc服务不加载！！！");
            }
        }
    }

    /**
     * 演示加载非riil数据库环境<br> 从its_db_config.json加载数据库
     *
     * @param dataSourceName 数据源名称，如：product.dataSource
     * @param mybatisFile    batis配置文件名称
     * @return 数据库对象
     */
    public static Db buildDb(final String dataSourceName, final String mybatisFile) {
        try {
            final Db db = GlobalRes.getDb(dataSourceName);
            db.initBatis(mybatisFile);
            return db;
        } catch (Exception e) {
            LOGGER.error("初始化AppDb失败", e);
            throw new IllegalStateException("初始化AppDb失败", e);
        }
    }

    public static DataSource getDataSource() {
        return db.getDataSource();
    }

    @SuppressWarnings("rawtypes")
    public static <S extends IBaseService> S getModelService(final Class<?> clazz) {
        return Services.getService(clazz, db);
    }

    public static <S extends IService> S getService(final Class<S> clazz) {
//        return Services.getService(clazz);
        return ServicesFactory.getService(clazz);
    }

    public static <S extends IBaseService> S getDBService(final Class<S> clazz) {
        return ServicesFactory.getService(clazz);
    }

    public static <S extends IService> S getService(final String serviceId, final Class<?> clazz) {
        return ServicesFactory.getService(serviceId, clazz);
    }

    public static <S extends IService> S getService(final int dcsId, final Class<?> clazz) {
        return ServicesFactory.getService(String.valueOf(dcsId), clazz);
    }


}
