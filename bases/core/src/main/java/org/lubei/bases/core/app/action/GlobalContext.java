package org.lubei.bases.core.app.action;

import org.lubei.bases.core.app.action.bean.proxy.BaseProxy;
import org.lubei.bases.core.app.action.loader.JsBeanLoader;
import org.lubei.bases.core.app.action.loader.NJsLoader;
import org.lubei.bases.core.app.action.loader.PluginLoader;
import org.lubei.bases.core.app.action.loader.ServiceBeanLoader;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * @author sany Created by sany on 17-4-19.
 */
public enum GlobalContext {

    INSTANCE;
    static final Logger LOGGER = LoggerFactory.getLogger(GlobalContext.class);

    LoadingCache<String, BaseProxy> beanCache;

    GlobalContext() {
        PluginLoader pluginLoader = new PluginLoader(null);
        ServiceBeanLoader<BaseProxy> serviceBeanLoader = new ServiceBeanLoader(pluginLoader);
        NJsLoader<BaseProxy> nJsLoader=new NJsLoader<BaseProxy>(serviceBeanLoader);
        JsBeanLoader<BaseProxy> jsBeanLoader = new JsBeanLoader<>(nJsLoader);

        beanCache = CacheBuilder.newBuilder().build(jsBeanLoader);
    }

    public BaseProxy getBean(String name) {
        try {
            return beanCache.get(name);
        } catch (ExecutionException e) {
            throw new IllegalStateException("获取底层支撑服务失败,服务名：" + name, e);
        }
    }

}
