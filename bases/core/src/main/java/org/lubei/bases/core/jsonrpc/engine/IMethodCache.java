package org.lubei.bases.core.jsonrpc.engine;

import org.lubei.bases.core.annotation.Audit;
import org.lubei.bases.core.annotation.BoolType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.cache.CacheStats;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

/**
 * 方法查找缓存接口
 */
public interface IMethodCache {

    /**
     * 获取key对应的方法对象
     *
     * @param key 类(或脚本）路径
     * @return 对应的方法对象
     * @throws ExecutionException 执行异常（缓存异常）
     */
    ClassMethod get(String key) throws ExecutionException;

    /**
     * 清理缓存
     */
    void clean();

    /**
     * 获取缓存状态
     *
     * @return 缓存状态
     */
    CacheStats getStats();

    ConcurrentMap<String, ClassMethod> ls();

    class ClassMethod {

        @Deprecated
        private Class clazz;
        @Deprecated
        private Method method;
        private String methodName;
        private AuditConf auditConf;
        private String endPoint;
        private Type[] parameterTypes;
        private boolean isStatic;
        private Process<Object[], Object> function;

        @Deprecated
        public ClassMethod(AuditConf auditConf, String endPoint, Class clazz, Method method) {
            this.endPoint = endPoint;
            this.clazz = clazz;
            this.method = method;
            this.parameterTypes = method.getGenericParameterTypes();
            this.isStatic = Modifier.isStatic(method.getModifiers());
            this.methodName = method.getName();
            if (auditConf == null) {
                prepareAuditConf();
            } else {
                this.auditConf = auditConf;
            }
        }

        public ClassMethod() {

        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("clazz", clazz)
                    .add("method", method)
                    .add("methodName", methodName)
                    .add("auditConf", auditConf)
                    .add("endPoint", endPoint)
                    .add("parameterTypes", parameterTypes)
                    .add("isStatic", isStatic)
                    .toString();
        }

        public AuditConf getAuditConf() {
            return auditConf;
        }

        public void setAuditConf(AuditConf auditConf) {
            this.auditConf = auditConf;
        }

        private void prepareAuditConf() {
            Audit clazzAnnotation = (Audit) clazz.getAnnotation(Audit.class);
            String clazzModule = null;
            BoolType clazzNoLogin = BoolType.NONE;
            if (clazzAnnotation != null) {
                clazzModule = clazzAnnotation.module();
                clazzNoLogin = clazzAnnotation.noLogin();
            }
            Audit methodAnnotation = method.getAnnotation(Audit.class);
            if (methodAnnotation != null) {
                auditConf = new AuditConf();
                if (!Strings.isNullOrEmpty(methodAnnotation.module())) {
                    auditConf.module = methodAnnotation.module();
                } else {
                    if (!Strings.isNullOrEmpty(clazzModule)) {
                        auditConf.module = clazzModule;
                    }
                }
                if (methodAnnotation.noLogin() == BoolType.NONE) {
                    auditConf.noLogin = clazzNoLogin;
                } else {
                    auditConf.noLogin = methodAnnotation.noLogin();
                }

                auditConf.type = methodAnnotation.type();
                auditConf.name = methodAnnotation.name();
                auditConf.request = methodAnnotation.request();
                auditConf.response = methodAnnotation.response();
                // 表达式？
            }

        }

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
        }

        @Deprecated
        public Class getClazz() {
            return clazz;
        }

        @Deprecated
        public Method getMethod() {
            return method;
        }

        public Type[] getParameterTypes() {
            return parameterTypes;
        }

        public void setParameterTypes(Type[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        public boolean isStatic() {
            return isStatic;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public Process<Object[], Object> getFunction() {
            return function;
        }

        public void setFunction(Process<Object[], Object> function) {
            this.function = function;
        }
    }
}
