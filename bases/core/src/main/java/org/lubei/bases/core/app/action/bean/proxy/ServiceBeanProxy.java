package org.lubei.bases.core.app.action.bean.proxy;

import org.lubei.bases.core.app.action.util.ParserUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class ServiceBeanProxy extends BaseProxy {

    Object target;
    Map<String, Method> methods = new HashMap<>();

    public ServiceBeanProxy(Object target) {
        this.target = target;
        initMethods(target);
    }

    private void initMethods(Object target) {
        Method[] declaredMethods = target.getClass().getDeclaredMethods();
        for (Method mothod : declaredMethods) {
            String key = mothod.getName() + ":" + mothod.getGenericParameterTypes().length;
            methods.put(key, mothod);
        }
    }

    public Object call(String method, Object... args) {
        Method method1 = methods.get(method + ":" + args.length);
        try {
            int length = method1.getGenericParameterTypes().length;
            for (int i = 0, size = args.length; i < size; i++) {
                args[i] = ParserUtil.to(args[i], method1.getParameterTypes()[i]);
            }
            return method1.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Class[] getArgumentTypes(String method) {
        Iterator<Map.Entry<String, Method>> iterator = methods.entrySet().iterator();
        Method methodReal = null;
        while (iterator.hasNext()) {
            Map.Entry<String, Method> next = iterator.next();
            // 当前rpc不支持调用的同一个类中存在相同方法名的情况
            if (next.getKey().startsWith(method + ":")) {
                methodReal = next.getValue();
                break;
            }
        }
        if (null != methodReal) {
            return methodReal.getParameterTypes();
        } else {
            return new Class[0];
        }
    }
}

