package org.lubei.bases.core.service;/**
 * Created by sany on 16-3-17.
 */

import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.Server;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class DistributedCenterService implements IPortable, IDistributedCenterService {

    static final Logger LOGGER = LoggerFactory.getLogger(DistributedCenterService.class);
    /**
     * 缓存映射Map.key为缓存ID，Value为缓存所在ServerId
     */
    private final Map<String, String> catchMap;
    /**
     * . service的内存缓存
     */
    private Table<String, Class, Object> serviceTable;

    public DistributedCenterService() {
        serviceTable = HashBasedTable.create();
        catchMap = new ConcurrentHashMap<>();
    }

    private <K, V> V get(LoadingCache<K, Optional<V>> cache, K key) {
        Optional<V> optional;
        try {
            optional = cache.get(key);
        } catch (ExecutionException e) {
            LOGGER.warn("", e);
            throw new RuntimeException("", e);
        }
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public Table<String, Class, Object> getServiceTable() {
        return serviceTable;
    }

    public void setServiceTable(Table<String, Class, Object> serviceTable) {
        this.serviceTable = serviceTable;
    }

    @Override
    public boolean contains(Class clazz) {
        return !getDistributedService(clazz).isEmpty();
    }

    @Override
    public IdentityHashMap<String, String> getDistributedService(Class clazz) {
        Class<? extends IDistributedCenterService> aClass = IDistributedCenterService.class;
        List<Server> servers = RemoteServices.getServers(aClass);
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        for (Server server : servers) {
            IDistributedCenterService service = RemoteServices.getService(server.getId(), aClass);
            map.putAll(service.getLocalServices(server.getId(), clazz));
        }
        return map;
    }

    @Override
    public IdentityHashMap<String, String> getLocalServices(String serverId, Class clazz) {
        IdentityHashMap<String, String> map = new IdentityHashMap<>();
        Stream<Map.Entry<Class, Object>> stream = serviceTable.row(serverId).entrySet().stream();
        stream = stream.filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass()));
        stream.forEach(entry -> map.put(entry.getValue().getClass().getName(), serverId));
        return map;
    }

    public Boolean existsOnServer(String catchId) {
        return GlobalRes.getCache(catchId) != null;
    }

    @Override
    public <T> T getCacheValue(String cacheId, Object key, Class<T> clazz)
            throws ExecutionException {
        if (this.existsOnServer(cacheId)) {//当前进程存在cache，优先本进程访问
            catchMap.put(cacheId, GlobalRes.getServerId());
            return getLocalCacheValue(cacheId, key, clazz);
        } else if (catchMap.containsKey(cacheId)) {//其他进程存在这个cache,按指定id访问
            try {
                IDistributedCenterService service = ServicesFactory.getService(
                        catchMap.get(cacheId), IDistributedCenterService.class);
                return service.getCacheValue(cacheId, key, clazz);
            } catch (Throwable e) {
                LOGGER.warn("获取缓存时发生异常,cacheid:{},key:{},class:{}", cacheId,
                            key, clazz, e);
                catchMap.remove(cacheId);//删除缓存server，重新获取一次,防止节点当机不切换
                return getCacheValue(cacheId, key, clazz);
            }
        } else { //本地cache不存在进行首次获取
            List<Server> servers = ServicesFactory.getServers(IDistributedCenterService.class);
            for (Server server : servers) {
                try {
                    IDistributedCenterService service = ServicesFactory.getService(
                            server.getId(), IDistributedCenterService.class);
                    if (service.existsOnServer(cacheId)) {//找到第一个server存在缓存
                        catchMap.put(cacheId, server.getId());
                        return service.getCacheValue(cacheId, key, clazz);
                    }
                } catch (Throwable e) {
                    LOGGER.trace("忽略获取cache所在server时失败.serverid:{},cacheid:{}", server.getId(),
                                 cacheId, e);
                }
            }
        }
        return null;
    }

    private <T> T getLocalCacheValue(String catchId, Object key, Class<T> clazz)
            throws ExecutionException {
        Cache cache = GlobalRes.getCache(catchId);
        if (cache == null) {
            return null;
        }
        if (cache instanceof LoadingCache) {
            return (T) get((LoadingCache) cache, key);
        } else {
            return (T) cache.getIfPresent(key);
        }
    }
}
