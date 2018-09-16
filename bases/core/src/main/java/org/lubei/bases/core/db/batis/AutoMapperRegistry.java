package org.lubei.bases.core.db.batis;

import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 带有自动加载机制的MapperRegistry.
 *
 * @author liwenheng@ruijie.com.cn
 */
public class AutoMapperRegistry extends MapperRegistry {

    static final Logger LOGGER = LoggerFactory.getLogger(AutoMapperRegistry.class);

    private Set addedPackages = Sets.newConcurrentHashSet();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public AutoMapperRegistry(Configuration config) {
        super(config);
    }

    /**
     * 替换注入SqlSessionFactory，使其具有自动加载mapper的能力.
     *
     * @param factory 事务工厂
     */
    public static void inject(SqlSessionFactory factory) {
        LOGGER.debug("开始注入");
        Configuration configuration = factory.getConfiguration();
        try {
            Field field = configuration.getClass().getDeclaredField("mapperRegistry");
            field.setAccessible(true);
            AutoMapperRegistry registry = new AutoMapperRegistry(configuration);
            field.set(configuration, registry);
            LOGGER.debug("注入成功");
        } catch (IllegalAccessException e) {
            LOGGER.warn("注入失败", e);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("注入失败", e);
        }
    }

    @Override
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        // 先判断所在包是否加载过，没有加载过先加载
        String packageName = Reflection.getPackageName(type);
        if (!addedPackages.contains(packageName)) {
            addMappers(packageName);
        }
        readWriteLock.readLock().lock();
        try {
            return super.getMapper(type, sqlSession);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public <T> boolean hasMapper(Class<T> type) {
        readWriteLock.readLock().lock();
        try {
            return super.hasMapper(type);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        try {
            super.addMapper(type);
        } catch (BindingException e) {
            LOGGER.warn("已经绑定过 {} {}", type, e.getMessage());
        }
    }

    @Override
    public Collection<Class<?>> getMappers() {
        return super.getMappers();
    }

    @Override
    public void addMappers(String packageName, Class<?> superType) {
        super.addMappers(packageName, superType);
    }

    @Override
    public void addMappers(String packageName) {
        readWriteLock.writeLock().lock();
        try {
            if (addedPackages.contains(packageName)) {
                return;
            }
            addedPackages.add(packageName);
            super.addMappers(packageName);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
