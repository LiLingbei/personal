package org.lubei.bases.core.app.action.loader;


import org.lubei.bases.core.app.action.GlobalContext;
import org.lubei.bases.core.app.action.bean.BeanAttributes;
import org.lubei.bases.core.app.action.bean.proxy.JSProxy;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class JsBeanLoader<V> extends BeanLoader<V> {

    public JsBeanLoader(BeanLoader next) {
        super(next);
    }

    @Override
    protected V getBean(Map config) {
        try {
            String string = (String) config.get(BeanAttributes.CONTENT);
            Context cx = Context.enter();
            String name = (String) config.get("name");
            Script script = cx.compileString(string, name, 1, null);
            ScriptableObject scope = cx.initStandardObjects();
            Object wrappedOut = Context.javaToJS(System.out, scope);
            Object gs = Context.javaToJS(GlobalContext.INSTANCE, scope);
            ScriptableObject.putProperty(scope, "out", wrappedOut);
            ScriptableObject.putProperty(scope, "gs", gs);
            Object exec = script.exec(cx, scope);
            return (V) new JSProxy(scope, cx);
        } finally {
            Context.exit();
        }
    }

    @Override
    protected boolean match(Map config) {
        return BeanAttributes.JS.equals(config.get(BeanAttributes.TYPE));
    }
}
