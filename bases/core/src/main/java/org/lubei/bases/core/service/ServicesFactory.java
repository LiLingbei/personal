package org.lubei.bases.core.service;

import org.lubei.bases.core.Server;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 服务工具工厂，提供服务的注册/反注册/服务对象获取方法。 根据内置的规则自动判断是远程服务还是本地服务。
 *
 * @author sany on 2015/5/13.
 */
public final class ServicesFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicesFactory.class);

    /**
     * 获取一个服务对象.
     *
     * @param clazz 服务接口类
     * @param <S>   服务接口类型,继承自IService
     * @return 服务对象实例
     */
    public static <S> S getService(final Class<S> clazz) {
        if (IBaseService.class.isAssignableFrom(clazz)) {
            LOGGER.warn("旧格式的服务 {}", clazz);
            return Services.getService(clazz);
        }
        S service = LocalServices.getService(clazz);
        if (service != null) {
            LOGGER.trace("本地服务获取 {}", clazz);
            return service;
        }
        service = RemoteServices.getService(clazz);
        if (service != null) {
            LOGGER.trace("远端服务获取 {}", clazz);
            return service;
        }
        LOGGER.warn("服务获取不到，将返回NULL {}", clazz);
        throw new NullPointerException("获取Service失败：" + clazz);
    }

    /**
     * 获取指定id的服务对象 目前支持远程rpc服务，其他服务后续按需扩展
     *
     * @param serviceId ：服务对象的Id
     * @param clazz     : 服务接口类
     * @param <S>       : 扩展自IService的接口
     * @return : 服务对象
     */
    public static <S> S getService(final String serviceId, final Class<?> clazz) {
        return RemoteServices.getService(serviceId, clazz);
    }

    /**
     * 注册服务
     *
     * @param clazz   : 服务接口类
     * @param service : 服务对象实例
     * @param <S>     : 服务接口类型
     */
    public static <S> void registerService(final Class<?> clazz, final S service) {
        if (isPortable(service)) {
            RemoteServices.registerService(clazz, service);
        } else {
            LocalServices.registerService(clazz, service);
        }
    }

    /**
     * 自动初始化并注册服务类所在的package中的所有实现IService的服务
     *
     * @param clazz 服务类
     */
    public static void registerServiceByClassPackage(Class<?> clazz) {
        String packageName = Reflection.getPackageName(clazz);
        registerServiceByPackage(packageName);
    }

    /**
     * 自动初始化并注册package中的所有实现IService的服务
     *
     * @param packageName 包名
     */
    public static void registerServiceByPackage(String packageName) {
        LOGGER.info("registerServiceByPackage {}", packageName);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 获取包内所有的顶级class（过滤掉私有class）
        ImmutableSet<ClassPath.ClassInfo> topLevelClasses;
        try {
            topLevelClasses = ClassPath.from(classLoader).getTopLevelClasses(packageName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Predicate<ClassPath.ClassInfo> predicate =
                input -> IService.class.isAssignableFrom(input.load());

        for (ClassPath.ClassInfo classInfo : FluentIterable.from(topLevelClasses)
                .filter(predicate)) {
            Class<?> load = classInfo.load();

            Class<?>[] interfaces = load.getInterfaces();
            Class<? extends IService> firstInterface = (Class<IService>) interfaces[0];
            if (firstInterface != null) {
                try {
                    IService o = (IService) load.newInstance();
                    ServicesFactory.registerService(firstInterface, o);
                    LOGGER.info("初始化加载 {} {}", load, firstInterface);
                } catch (Throwable t) {
                    LOGGER.error("加载服务失败 {}", load, t);
                }
            }
        }
    }

    /**
     * 反注册服务.
     *
     * @param clazz   :服务接口类
     * @param service :服务对象实例
     * @param <S>     : 服务接口类型,继承自IService
     */
    public static <S> void unregisterService(final Class<?> clazz, final S service) {
        if (isPortable(clazz)) {
            RemoteServices.unregisterService(clazz, service);
        } else {
            LocalServices.unregisterService(clazz, service);
        }
    }

    private static boolean isPortable(final Object service) {
        return service instanceof IPortable;
    }

    public static boolean contains(final Class<?> clazz) {
        return RemoteServices.contains(clazz) || LocalServices.contains(clazz);
    }

    public static List<Server> getServers(Class<?> clazz) {
        return RemoteServices.getServers(clazz);
    }

    /**
     * 获取指定接口注册的多个实现类的rpc句柄
     *
     * @param clazz 接口或抽象类
     * @return 注册该接口的Map列表，key为serverid,value为实现类
     */
    public static Map<String, String> getRemoteServices(Class<?> clazz) {
        return RemoteServices.getServices(clazz);
    }

    private ServicesFactory() {
    }
}
