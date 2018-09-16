package org.lubei.bases.core.app.action.loader;

import org.lubei.bases.core.app.action.bean.BeanAttributes;

import java.util.Map;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class PluginLoader<V> extends BeanLoader<V> {

    public PluginLoader(BeanLoader next) {
        super(next);
    }

    @Override
    protected V getBean(Map config) {
        return null;
    }

    @Override
    protected boolean match(Map config) {
        return BeanAttributes.PLUGIN.equals(config.get(BeanAttributes.TYPE));
    }
}
