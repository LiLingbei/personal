package org.lubei.bases.core.task;

import org.lubei.bases.core.annotation.ExcludeAutoWare;
import org.lubei.bases.core.jsonrpc.engine.AppException;
import org.lubei.bases.core.service.IService;
import org.lubei.bases.core.service.ServicesFactory;
import org.lubei.bases.core.util.GlobalEventBus;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

/**
 * 一直运行的任务
 *
 * @author hongliangpan@gmail.com
 */
public abstract class AbstractDaemon extends AbstractIdleService implements IService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractDaemon.class);
    private static final String[] FILTERED_PACKAGES = new String[]{
            "java",
            "com.google.common",
            org.lubei.bases.core.service.IPortable.class.getPackage().getName()
    };
    private static final int AUTO_DI_TIMEOUT = 60 * 60 * 1000;
    protected Map<String, Service> serviceMap = Maps.newLinkedHashMap();

    /**
     * 自动获取daemon所在包下所有Service列表。 如果需要特殊Service控制,需覆盖该方法.
     */
    public List<Class> getServices() {
        // 按照包名搜索所有Service
        String packageName = Reflection.getPackageName(this.getClass());
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ImmutableSet<ClassPath.ClassInfo> topLevelClasses;
        try {
            topLevelClasses = ClassPath.from(loader).getTopLevelClassesRecursive(packageName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        List<Class> excludeServices = Lists.newArrayList();
        List<Class> services = Lists.newArrayList();
        for (ClassPath.ClassInfo classInfo : topLevelClasses) {
            Class clazz = classInfo.load();
            if (clazz.isInterface() || clazz.isEnum() || !IService.class.isAssignableFrom(clazz)
                || clazz.equals(this.getClass())) {
                continue;
            }
            if (clazz.isAnnotationPresent(ExcludeAutoWare.class)) {
                excludeServices.add(clazz);
                continue;
            }
            services.add(clazz);
        }
        LOGGER.info("本次手工排除注册启动的服务:{} ", excludeServices);
        return services;

    }

    /**
     * 自动初始化daemon所在包下的所有服务
     */
    protected void initServiceAuto() {
        List<Class> services = getServices();
        List<Class> dependenceServices = Lists.newArrayList();
        for (Class clazz : services) {
            if (!isDependenceSatisfy(clazz)) {
                dependenceServices.add(clazz);
                continue;
            }
            try {
                initService(clazz);
            } catch (Throwable t) {
                LOGGER.error("加载服务失败 {}", clazz, t);
            }
        }
        LOGGER.debug("开始创建依赖服务...");
        initDependenceService(dependenceServices);
        LOGGER.debug("完成创建依赖服务...");
        if (dependenceServices.size() > 0) {
            autoScanDependenceService(dependenceServices);
        }
    }

    private void autoScanDependenceService(List<Class> dependenceServices) {

        final long start = System.currentTimeMillis();
        AbstractExecutionThreadService diService = new AbstractExecutionThreadService() {
            @Override
            protected void run() throws Exception {
                boolean finish = true;
                while (isRunning()) {
                    LOGGER.debug("5秒后重新检查服务依赖");
                    try {
                        Thread.currentThread().sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initDependenceService(dependenceServices);
                    long span = System.currentTimeMillis() - start;
                    finish = dependenceServices.size() > 0 || span < AUTO_DI_TIMEOUT;
                    if (!finish) {
                        break;
                    }
                }
                if (dependenceServices.size() > 0) {
                    LOGGER.error("持续运行1小时后，部分依赖仍然不满足:{}", dependenceServices);
                }
            }

            @Override
            protected String serviceName() {
                return "AbstractDaemonAutoDI";
            }
        };
        diService.startAsync();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                diService.stopAsync();
            }
        });
    }

    /**
     * 查找要暴露的服务接口
     *
     * 过滤掉core、java、guava等接口
     *
     * @param clazz 服务类
     * @return 服务接口（可能为空）
     */
    private Class<?> getServiceInterface(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            String name = anInterface.getName();
            for (int i = 0; i < FILTERED_PACKAGES.length; i++) {
                if (!name.startsWith(FILTERED_PACKAGES[i])) {
                    return anInterface;
                }
            }
        }
        return null;
    }

    private void initService(Class clazz) throws InstantiationException, IllegalAccessException {
        Class<?> serviceInterface = getServiceInterface(clazz);
        IService instance = (IService) clazz.newInstance();
        if (isSubscribePresent(clazz)) {
            GlobalEventBus.INSTANCE.register(instance);
        }
        if (serviceInterface != null) {
            ServicesFactory.registerService(serviceInterface, instance);
            LOGGER.debug("初始化加载 {} {}", clazz, serviceInterface);
        }
        if (instance instanceof Service) {
            LOGGER.debug("即将启动服务{} ", clazz);
            try {
                ((Service) instance).startAsync().awaitRunning(60000, TimeUnit.MILLISECONDS);
                LOGGER.debug("完成启动服务{} ", clazz);
                serviceMap.put(clazz.getSimpleName(), (Service) instance);
            } catch (TimeoutException e) {
                LOGGER.warn("服务{}未在60s内启动，可能存在问题...", clazz, e);
                throw new AppException("服务" + clazz.getName() + "启动失败");
            }
        }
    }

    private boolean isSubscribePresent(Class clazz) {
        Method[] methods = clazz.getMethods();
        return Lists.newArrayList(methods).stream()
                .anyMatch(method -> method.isAnnotationPresent(Subscribe.class));
    }

    private boolean isDependenceSatisfy(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        boolean haDi = true;
        for (Field field : fields) {
            Class<?> injectClass = field.getType();
            if (field.isAnnotationPresent(Inject.class) && !ServicesFactory.contains(injectClass)) {
                LOGGER.info("服务{}存在依赖项{}未注册或启动 ...", clazz, injectClass);
                haDi = false;
                break;
            }
        }
        return haDi;
    }

    private void initDependenceService(List<Class> dependences) {
        int startSize = dependences.size();
        if (startSize == 0) {
            return;
        }
        for (int i = startSize - 1; i >= 0; i--) {
            Class clazz = dependences.get(i);
            if (!isDependenceSatisfy(clazz)) {
                continue;
            }
            try {
                initService(clazz);
            } catch (Exception e) {
                LOGGER.error("加载服务失败 {}", clazz, e);
            }
            dependences.remove(clazz);
        }
        if (dependences.size() < startSize) {
            initDependenceService(dependences);
        } else {
            LOGGER.error("本次未能正常启动的服务:{}", dependences);
        }
    }

    @Override
    protected void startUp() throws Exception {
        initServiceAuto();
    }

    @Override
    protected void shutDown() throws Exception {
        LOGGER.info("shutDown ccs...");
        for (Map.Entry<String, Service> entry : serviceMap.entrySet()) {
            entry.getValue().stopAsync();
        }
        serviceMap.clear();
    }
}
