package org.lubei.bases.core.cache;

import org.lubei.bases.core.task.ScheduledTask;
import org.lubei.bases.core.util.Metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 */
public class CacheManager {

    static final Logger LOGGER = LoggerFactory.getLogger(CacheManager.class);
    /**
     * 缓存清理周期，单位分钟
     */
    private static final long LOOP_INTERVAL = 5;
    private final Map<String, CacheBean> caches;
    /**
     * 缓存清理服务，周期清理已失效的缓存
     */
    private CleanService cleanService;

    public CacheManager() {
        caches = Maps.newConcurrentMap();
        cleanService = new CleanService();
        cleanService.startAsync();
    }

    @Override
    public String toString() {
        ToStringHelper s = MoreObjects.toStringHelper(CacheManager.class);
        for (Entry<String, CacheBean> ent : caches.entrySet()) {
            s.add(ent.getKey(), ent.getValue());
        }
        return s.toString();
    }

    /**
     * 注册缓存
     *
     * @param register 注册者
     * @param id       唯一标识
     * @param cache    缓存
     * @return 缓冲（原样返回）
     */
    @SuppressWarnings("rawtypes")
    public synchronized Cache registerCache(final String register, final String id,
                                            final Cache cache) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkArgument(!caches.containsKey(id), "already registered {}", id);
        LOGGER.info("registerCache {} {} {}", id, register, cache);
        caches.put(id, new CacheBean(cache, register));
        try {
            Metrics.REGISTRY.register(MetricRegistry.name("cache", id, "size"),
                                      (Gauge<Long>) () -> cache.size());
            Metrics.REGISTRY.register(MetricRegistry.name("cache", id, "hitRate"),
                                      (Gauge<Double>) () -> cache.stats().hitRate());
        } catch (Throwable t) {
            LOGGER.warn("注册指标失败", t);
        }
        return cache;
    }

    @SuppressWarnings("rawtypes")
    public synchronized Cache getCache(final String id) {
        CacheBean cacheBean = caches.get(id);
        if (null == cacheBean) {
            return null;
        }
        return cacheBean.getCache();
    }

    /**
     * 清理缓存
     */
    private void cleanUpCaches() {
        for (Entry<String, CacheBean> ent : caches.entrySet()) {
            ent.getValue().cache.cleanUp();
        }
    }

    /**
     * 启动缓存清理服务
     */
    public void startCacheCleaner() {
        cleanService.startAsync();
    }

    /**
     * 关闭缓存清理服务
     */
    public void shutdownCacheCleaner() {
        cleanService.stopAsync();
    }

    private static class CacheBean {

        @SuppressWarnings({"rawtypes"})
        private final Cache cache;
        private final String register;
        private final Date registerTime = new Date();

        public CacheBean(@SuppressWarnings("rawtypes") final Cache cache, final String register) {
            super();
            this.cache = cache;
            this.register = register;
        }

        /**
         * @return cache -cache
         */
        @SuppressWarnings("rawtypes")
        public final Cache getCache() {
            return cache;
        }

        @Override
        public String toString() {
            ToStringHelper s = MoreObjects.toStringHelper(this);
            s.add("register", register);
            s.add("registerTime", registerTime);
            return s.toString();
        }


    }

    private class CleanService extends ScheduledTask {

        @Override
        protected void runOneIteration() throws Exception {
            cleanUpCaches();
        }

        @Override
        protected AbstractScheduledService.Scheduler scheduler() {
            return AbstractScheduledService.Scheduler
                    .newFixedRateSchedule(LOOP_INTERVAL, LOOP_INTERVAL, TimeUnit.MINUTES);
        }
    }
}
