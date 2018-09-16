package org.lubei.bases.core.app.action.loader;

import org.lubei.bases.core.app.action.GlobalContext;
import org.lubei.bases.core.app.action.bean.BeanAttributes;
import org.lubei.bases.core.app.action.bean.proxy.NjsProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author sany Created by sany on 17-5-26.
 */
public class NJsLoader<V> extends BeanLoader<V> {

    static final Logger LOGGER = LoggerFactory.getLogger(NJsLoader.class);

    public NJsLoader(BeanLoader next) {
        super(next);
    }

    @Override
    protected V getBean(Map config) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try {
            engine.put("gs", GlobalContext.INSTANCE);//不能使用binding，否则无法获取函数;
            Object eval = engine.eval((String) config.get(BeanAttributes.CONTENT));//,simpleBindings);
            Invocable invocable = (Invocable) engine;
            return (V) new NjsProxy(invocable, eval);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected boolean match(Map config) {
        return BeanAttributes.NJS.equals(config.get(BeanAttributes.TYPE));
    }
}
