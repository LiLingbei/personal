package org.lubei.bases.core.app.action.loader;

import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.app.action.bean.BeanAttributes;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author sany Created by sany on 17-4-26.
 */
public abstract class BeanLoader<V> extends CacheLoader<String, V> {

    /**
     * ci_script缓存名
     */
    private static final String CI_SCRIPT_CACHE_NAME = "relax.cache.ci.script";
    static final Logger LOGGER = LoggerFactory.getLogger(BeanLoader.class);
    private BeanLoader<V> next;
    private LoadingCache<String, Map<String, Object>> actionCache;

    BeanLoader(BeanLoader next) {
        actionCache = (LoadingCache<String, Map<String, Object>>) GlobalRes
                        .getCache(CI_SCRIPT_CACHE_NAME);
        this.next = next;
    }

    public BeanLoader getNext() {
        return next;
    }

    public void setNext(BeanLoader next) {
        this.next = next;
    }

    @Override
    public V load(String key) throws Exception {
        try {
            Map config = getConfig(key);
            BeanLoader<V> loader = this;
            while (loader != null) {
                if (loader.match(config)) {
                    return loader.getBean(config);
                }
                loader = loader.getNext();
            }
            LOGGER.warn("加载的服务{}不存在", key);
            throw new IllegalArgumentException("BeanLoader 找不到指定名称：" + key);
        } catch (Exception e) {
            LOGGER.warn("加载的服务{}不存在", key, e);
            throw new IllegalArgumentException("BeanLoader 找不到指定名称：" + key, e);
        }
    }

    private Map getConfig(String key) {
        try {
            //rpcservice 需要如何配置?
            Map<String, Object> ifPresent = actionCache.getIfPresent(key);
            if (ifPresent != null) {
                return ifPresent;
            }
            //try load
            try {
                actionCache.refresh(key);
                ifPresent = actionCache.getIfPresent(key);
                if (ifPresent != null) {
                    return ifPresent;
                }
            } catch (Exception e) {
                LOGGER.debug("数据库中找不到服务{}配置信息,继续查找内存", key, e);
            }

            // 如果是其他情况，默认作为rpc 服务来使用
            Map<String, Object> config = Maps.newHashMap();
            config.put(BeanAttributes.TYPE, BeanAttributes.RPC);
            config.put(BeanAttributes.CLASS, key);
            actionCache.put(key, config);//线程不安全，但是影响不大
            return actionCache.get(key); //TODO需要明确格式
        } catch (ExecutionException e) {
            throw new IllegalStateException("访问的服务配置不存在:" + key, e);
        }
    }

    protected abstract V getBean(Map config);

    protected abstract boolean match(Map config);
}
