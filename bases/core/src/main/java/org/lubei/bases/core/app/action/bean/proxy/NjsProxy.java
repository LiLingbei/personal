package org.lubei.bases.core.app.action.bean.proxy;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 * @author sany Created by sany on 17-5-26.
 */
public class NjsProxy extends BaseProxy {

    static final Logger LOGGER = LoggerFactory.getLogger(NjsProxy.class);
    private final Invocable invocable;
    private final Object target;

    public NjsProxy(Invocable invocable, Object target) {
        this.invocable = invocable;
        this.target = target;
    }

    @Override
    public Object call(String method, Object... args) {

        try {
//            Object o = ((ScriptEngine) invocable).get(method);
//            ((ScriptEngine)invocable).setContext((ScriptContext) target);
            return invocable.invokeFunction(method, args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Class[] getArgumentTypes(String method) {
        int length =
                (int) ((ScriptObjectMirror) ((ScriptEngine) invocable).getContext()
                        .getAttribute(method))
                        .getMember("length");
        Class[] classes = new Class[length];
        for (int i = 0; i < length; i++) {
            // NativeObject 在带有参数的js方法中不可执行,JSONObject只适用于Json格式
            classes[i] = Map.class;
        }
        return classes;
    }
}
