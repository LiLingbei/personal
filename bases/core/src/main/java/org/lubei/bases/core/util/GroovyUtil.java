package org.lubei.bases.core.util;

import com.google.common.base.Preconditions;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Groovy工具类，提供从classpath加载脚本及调用静态方法等功能
 */
public class GroovyUtil {

    private static final Logger logger = LoggerFactory.getLogger(GroovyUtil.class);

    private GroovyUtil() {
        throw new IllegalAccessError("不能实例化");
    }

    /**
     * 从classpath根路径下加载groovy脚本资源
     *
     * @param resourceName 脚本资源文件名称
     * @param resourceName 脚本资源文件名称
     * @return 编译加载后的groovy脚本类
     */
    @SuppressWarnings("rawtypes")
    public static Class loadClass(final String resourceName) {
        return loadClass((Class<?>) null, resourceName);
    }

    /**
     * 从classpath加载groovy脚本资源
     *
     * @param contextClass 上下文class，脚本在该类同路径下
     * @param resourceName 脚本资源文件名称
     * @return 编译加载后的groovy脚本类
     */
    @SuppressWarnings("rawtypes")
    public static Class loadClass(final Class<?> contextClass, final String resourceName) {
        logger.info("尝试加载插件 {}", resourceName);
        String content;
        try {
            if (contextClass == null) {
                content = ResourceUtil.getString(resourceName);
            } else {
                content = ResourceUtil.getString(contextClass, resourceName);
            }
        } catch (Throwable t) {
            logger.error("插件加载失败：文件IO错误", t);
            return null;
        }
        Preconditions.checkNotNull(content);
        try {
            Class clazz = loadClass(resourceName, content);
            logger.info("插件加载成功");
            return clazz;
        } catch (Throwable t) {
            logger.error("插件加载失败：文件IO错误", t);
            return null;
        }
    }

    /**
     * 从资源文件加载GROOVY类
     *
     * @param resourceName 资源文件名
     * @param content      文件内容
     * @return 资源文件编译出的主类
     */
    @SuppressWarnings("rawtypes")
    public static Class loadClass(final String resourceName, final String content) {
        GroovyCodeSource gcs = new GroovyCodeSource(content, resourceName, "UTF-8");
        PrivilegedAction<GroovyClassLoader> privilegedAction =
                () -> new GroovyClassLoader(GroovyUtil.class.getClassLoader(),
                                            CompilerConfiguration.DEFAULT);
        GroovyClassLoader loader = AccessController.doPrivileged(privilegedAction);
        Class clazz = loader.parseClass(gcs);
        return clazz;
    }

    /**
     * 调用类的静态方法
     *
     * @param clazz  所调静态方法的类
     * @param method 方法名
     * @param params 参数
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    public static Object invokeStaticMethod(Class<?> clazz, String method, Object[] params)
            throws Throwable {
        try {
            Object ret = InvokerHelper.invokeStaticMethod(clazz, method, params);
            return ret;
        } catch (Throwable t) {
            logger.error("invokeStaticMethod调用失败", t);
            throw t;
        }
    }

    public static Printer printer(Logger logger) {
        return new LoggerPrinter(logger);
    }

    public static Printer printer(StringBuilder stringBuilder) {
        return new StringBuilderPrinter(stringBuilder);
    }

    public interface Printer {

        void println();

        void print(Object value);

        void println(Object value);

        Printer printf(String format, Object value);

        Printer printf(String format, Object[] values);
    }

    /**
     * Logger 打印器，Groovy Script 的 out 使用
     */
    public static class LoggerPrinter implements Printer {

        Logger logger;


        public LoggerPrinter(Logger logger) {
            this.logger = logger;
        }

        public void println() {
            // do nothing
        }

        public void print(Object value) {
            logger.debug("{}", value);
        }

        public void println(Object value) {
            logger.debug("{}", value);
        }

        public Printer printf(String format, Object value) {
            println(String.format(format, value));
            return this;
        }

        public Printer printf(String format, Object[] values) {
            println(String.format(format, values));
            return this;
        }

    }

    /**
     * Logger 打印器，Groovy Script 的 out 使用
     */
    public static class StringBuilderPrinter implements Printer {

        StringBuilder stringBuilder;


        public StringBuilderPrinter(StringBuilder stringBuilder) {
            this.stringBuilder = stringBuilder;
        }

        public void println() {
            stringBuilder.append("\r");
        }

        public void print(Object value) {
            stringBuilder.append(String.valueOf(value));
        }

        public void println(Object value) {
            print(value);
            println();
        }

        public Printer printf(String format, Object value) {
            println(String.format(format, value));
            return this;
        }

        public Printer printf(String format, Object[] values) {
            println(String.format(format, values));
            return this;
        }

    }
}
