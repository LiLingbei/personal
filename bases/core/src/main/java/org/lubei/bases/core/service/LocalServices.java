package org.lubei.bases.core.service;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 本地服务工具类，提供纯本进程的服务得注册、反注册和类实例获取. <p/> Created by sany on 2015/5/13.
 */
public final class LocalServices {

    /**
     * 日志工具
     */
    private static Logger LOGGER = LoggerFactory.getLogger(LocalServices.class);
    /**
     * . service的内存缓存
     */
    private static Map<Object, IService> serviceMap;

    static {
        serviceMap = Maps.newConcurrentMap();
    }

    /**
     * 防止创建实例
     */
    private LocalServices() {

    }

    /**
     * 获取一个服务.如果服务已经注册返回服务对象，否则返回null
     *
     * @param clazz : 服务接口类
     * @param <S>   : 服务接口类型
     * @return : 服务接口类对象
     */
    public static <S extends IService> S getService(final Class<?> clazz) {
        S service = (S) serviceMap.get(clazz);
        return service;
    }

    /**
     * 注册一个本地服务
     *
     * @param clazz   : 服务接口类
     * @param service : 服务对象
     * @param <S>     : 服务接口类型
     */
    public static <S> void registerService(final Class<?> clazz, final S service) {
        LOGGER.debug("registerService 接口:{}, 实例:{}", clazz, service);
        if (!serviceMap.containsKey(clazz)) {
            serviceMap.put(clazz, (IService) service);
        } else {
            if (serviceMap.get(clazz).equals(service)) {
                LOGGER.info("服务{}已经注册，无需重复注册", service.getClass().getName());
            }
        }
    }

    /**
     * 反注册一个本地服务
     *
     * @param clazz   : 服务接口类
     * @param service : 服务对象
     * @param <S>     : 服务接口类型
     */
    public static <S> void unregisterService(final Class<?> clazz,
                                             final S service) {
        if (serviceMap.containsKey(clazz)) {
            serviceMap.remove(clazz);
        }
    }

    public static boolean contains(Class<?> clazz) {
        return serviceMap.containsKey(clazz);
    }
}
