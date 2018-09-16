package org.lubei.bases.core.app.action.loader;

import org.lubei.bases.core.app.action.bean.BeanAttributes;
import org.lubei.bases.core.app.action.bean.proxy.ServiceBeanProxy;
import org.lubei.bases.core.service.ServicesFactory;

import java.util.Map;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class ServiceBeanLoader<V> extends BeanLoader<V> {


    public ServiceBeanLoader(BeanLoader next) {
        super(next);
    }

    @Override
    protected V getBean(Map config) {
        //自动选择bean
        Class clazz = null;
        String name = (String) config.get(BeanAttributes.CLASS);
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("系统环境中无此无服务，服务名:" + name, e);
        }
        Object service = ServicesFactory.getService(clazz);
        return (V) new ServiceBeanProxy(service);
    }

    @Override
    protected boolean match(Map config) {
        return BeanAttributes.RPC.equals(config.get(BeanAttributes.TYPE));
    }


}
