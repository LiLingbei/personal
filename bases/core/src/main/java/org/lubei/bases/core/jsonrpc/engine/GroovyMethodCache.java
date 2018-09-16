package org.lubei.bases.core.jsonrpc.engine;

import com.google.common.base.Preconditions;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * 基于groovy和class的方法查找缓存
 */
public class GroovyMethodCache extends ClassMethodCache {

    private static final String GROOVY = ".groovy.";
    private static final int SUFMIX_LEN = GROOVY.length();

    GroovyScriptEngine engine;

    public GroovyMethodCache(GroovyScriptEngine engine) {
        this.engine = engine;
    }

    @Override
    ClassMethod findMethod(String endPoint) {
        int groovyIndex = endPoint.indexOf(GROOVY);
        if (groovyIndex == -1) {
            return super.findMethod(endPoint);
        }

        String method = endPoint.substring(groovyIndex + SUFMIX_LEN, endPoint.length());
        String path = endPoint.substring(0, groovyIndex + SUFMIX_LEN - 1);
        Class aClass;
        try {
            aClass = engine.loadScriptByName(path);
        } catch (ResourceException e) {
            throw new IllegalAccessError("无效的路径" + path + ", 原因：" + e.getMessage());
        } catch (ScriptException e) {
            throw new IllegalAccessError("脚本解析错误" + path + ", 原因：" + e.getMessage());
        }
        ClassMethod classMethod = getClassMethod(endPoint, method, aClass);
        return Preconditions.checkNotNull(classMethod, "方法未找到%s", endPoint);
    }

}
