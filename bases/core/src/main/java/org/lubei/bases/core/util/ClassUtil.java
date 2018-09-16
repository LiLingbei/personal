package org.lubei.bases.core.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 类搜索工具类
 *
 * @author gaojuhua.
 */
public class ClassUtil {

    private static final String PACKAGE_NAME = "com.its";

    /**
     * 获取所有子类或接口实现类
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls) {
        return getAllAssignedClass(cls, PACKAGE_NAME);
    }

    /**
     * 获取所有子类或接口实现类
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls, String packageName) {
        List<Class<?>> classes = new ArrayList<Class<?>>();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ImmutableSet<ClassPath.ClassInfo> topLevelClasses = null;
        try {
            topLevelClasses = ClassPath.from(loader).getTopLevelClassesRecursive(packageName);
        } catch (IOException e) {
            // do nothing
        }
        for (ClassPath.ClassInfo classInfo : topLevelClasses) {
            try {
                Class clazz = classInfo.load();
                if (cls.isAssignableFrom(clazz) && !cls.equals(clazz)) {
                    classes.add(clazz);
                }
            } catch (Throwable e) {
                // do nothing
            }

        }
        return classes;

    }


}
