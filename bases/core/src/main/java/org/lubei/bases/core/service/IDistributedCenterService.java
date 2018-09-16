package org.lubei.bases.core.service;

import java.util.IdentityHashMap;
import java.util.concurrent.ExecutionException;

/**
 * Created by sany on 16-3-21.
 */
public interface IDistributedCenterService {

    boolean contains(Class clazz);

    /**
     * 获取整个分布系统上的所有注册某一个类型的的注册实例映射.
     * 只要用于一个接口多个实现类的情况如何全部调用。
     *
     * @param clazz ：注册用的接口.
     * @return :key为实例类名，value为serverId的Map,key为可重复的。只能遍历.
     */
    IdentityHashMap<String, String> getDistributedService(Class clazz);

    /**
     * 获取指定server的指定接口所在实例类名和serverId映射.
     *
     * @param serverId :server的id.
     * @param clazz    :指定接口名.
     * @return ：key为实例类名，value为serverId的Map,key为可重复的。只能遍历.
     */
    IdentityHashMap<String, String> getLocalServices(String serverId, Class clazz);

    /**
     * 根据指定CacheId和缓存键值，获取指定的cache对象.
     *
     * @param cacheId : Cache的id，用于Cache的注册和获取一般在pojo模块中进行常量定义。
     * @param key     :缓存键值，一般为对象id或者为对象唯一键值.
     * @param clazz   :缓存对象类型
     * @param <T>     :缓存对象类型
     * @return 存在返回缓存对象，否则返回null
     * @throws ExecutionException ：当缓存实现获取时返回异常.
     */
    <T> T getCacheValue(String cacheId, Object key, Class<T> clazz) throws ExecutionException;

    /**
     * 判断指定Cache是否在改在当前进程中存在。
     *
     * @param catchId :Cache的id，用于Cache的注册和获取一般在pojo模块中进行常量定义。
     * @return 是否在存在，存在返回true;反之false;
     */
    Boolean existsOnServer(String catchId);
}
