package org.lubei.bases.core.service.loader;

import org.lubei.bases.core.service.IService;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 
 * 服务管理，cache <br>
 * 
 * <p>
 * Create on : 2014-4-28<br>
 * <p>
 * </p>
 * <br>
 * 
 * @author panhongliang<br>
 * @version its-core v1.3.3
 *          <p>
 *          <br>
 *          <strong>Modify History:</strong><br>
 *          user modify_date modify_content<br>
 *          -------------------------------------------<br>
 *          <br>
 */
@Deprecated
public class ServiceManager<S extends IService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManager.class);

    // private final Map<Class<S>, S> serviceMap;
    public final LoadingCache<Class<S>, S> serviceCache;

    public ServiceManager() {
        // serviceMap = Maps.newConcurrentMap();
        // 初始化私有资源
        CacheLoader<Class<S>, S> serviceLoader = new CacheLoader<Class<S>, S>() {
            @Override
            public S load(final Class<S> clazz) throws Exception {
                // 配置优先
                S service = loadByConfigFile(clazz);
                if (null == service) {
                    // 默认规则
                    service = loadByDefaultRule(clazz);
                }
                if (null == service) {
                    // 默认规则
                    service = loadByDefaultRuleCurrentContext(clazz);
                }

                if (null == service) {
                    LOGGER.error("加载服务异常{}", clazz);
                }
                return service;
            }
        };
        serviceCache = CacheBuilder.newBuilder().build(serviceLoader);
    }

    public void registerService(final Class<S> clazz, final S service) {
        serviceCache.put(clazz, service);
    }

    public S getService(final Class<S> clazz) {
        try {
            return serviceCache.get(clazz);
        } catch (Exception e) {
            if (!e.getMessage().startsWith("CacheLoader returned null for key")) {
                LOGGER.warn("加载服务异常", e);
            }
            return null;
        }
    }



    @SuppressWarnings("unchecked")
    private S loadByDefaultRuleCurrentContext(final Class<S> clazz) {
        String name = clazz.getSimpleName();
        String implClassName = clazz.getName();
        if (name.startsWith("I")) {
            name = name.substring(1);
            implClassName = clazz.getPackage().getName() + ".impl." + name;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            if("java.lang.Object".equals(implClassName)){
                LOGGER.info("加载服务   loadByDefaultRule " + implClassName );
            }
            S service = (S) Class.forName(implClassName, true, loader).newInstance();
            LOGGER.info("加载服务   loadByDefaultRule " + implClassName + "    " + service);
            return service;
        } catch (Exception e) {
            LOGGER.error("加载服务异常{}",loader);
            LOGGER.error("加载服务异常   loadByDefaultRule   " + implClassName + e.getMessage(), e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private S loadByDefaultRule(final Class<S> clazz) {
        String name = clazz.getSimpleName();
        String implClassName = clazz.getName();
        if (name.startsWith("I")) {
            name = name.substring(1);
            implClassName = clazz.getPackage().getName() + ".impl." + name;
        }
        try {
            S service = (S) Class.forName(implClassName).newInstance();
            LOGGER.info("加载服务   loadByDefaultRule " + implClassName + "    " + service);
            return service;
        } catch (Exception e) {
            LOGGER.error("加载服务异常   loadByDefaultRule   " + implClassName + e.getMessage(), e);
            return null;
        }
    }

    /**
     * loadByConfigFile.
     * 
     * @param clazz String
     */
    private S loadByConfigFile(final Class<S> clazz) {
        ServiceLoader<S> serviceLoader = ServiceLoader.load(clazz);
        Iterator<S> searchs = serviceLoader.iterator();
        // TODO 发现多个服务，错误处理提示
        if (searchs.hasNext()) {
            final S service = searchs.next();
            LOGGER.info("加载服务   loadByConfigFile   " + service);
            return service;
        }
        return null;
    }



}
