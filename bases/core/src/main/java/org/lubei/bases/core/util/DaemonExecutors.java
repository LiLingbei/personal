package org.lubei.bases.core.util;

import static com.codahale.metrics.MetricRegistry.name;
import static org.lubei.bases.core.util.jmx.JmxHelper.M_BEAN_SERVER;
import static org.lubei.bases.core.util.jmx.JmxHelper.NAME_FACTORY;

import org.lubei.bases.core.util.jmx.ThreadPoolStatus;

import com.codahale.metrics.Gauge;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * 守护线程池工具类
 *
 * 用来创建带名字且打印未捕获异常日志的线程池
 */
public class DaemonExecutors {

    public static final String TYPE = "DaemonExecutor";
    public static final String DOMAIN = "its";
    static final Comparator<Runnable> COMPARATOR = new Comparator<Runnable>() {
        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (o1 instanceof PriorityRunnable) {
                PriorityRunnable priorityRunnable1 = (PriorityRunnable) o1;
                if (o2 instanceof PriorityRunnable) {
                    PriorityRunnable priorityRunnable2 = (PriorityRunnable) o2;
                    return Integer.compare(priorityRunnable1.priority, priorityRunnable2.priority);
                }
                return 1;
            }
            if (o2 instanceof PriorityRunnable) {
                return -1;
            }
            return 0;
        }
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(DaemonExecutors.class);

    /**
     * 创建守护线程工厂
     *
     * @param name 线程名字前缀，产生的线程名称为"name_%d"
     * @return 线程工厂
     */
    private static ThreadFactory newThreadFactory(String name) {
        return new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("back-" + name + "_%d")
                .setUncaughtExceptionHandler((t, e) -> LOGGER.warn("thread exception", e)).build();
    }

    /**
     * 创建单线程的线程池
     *
     * @param name 线程名称前缀
     * @return 线程池
     */
    public static ExecutorService newSingleThreadExecutor(String name) {
        return newFixedThreadPool(name, 1); // 改为使用固定线程池，这样才能统计性能信息
    }

    /**
     * 创建固定线程数量的线程池
     *
     * @param name 线程名称前缀
     * @param i    线程数量
     * @return 线程池
     */
    public static ExecutorService newFixedThreadPool(String name, int i) {
        ThreadFactory threadNormalFactory = newThreadFactory(name);
        ExecutorService executorService = Executors.newFixedThreadPool(i, threadNormalFactory);
        register(executorService, name);
        return executorService;
    }

    /**
     * 创建固定线程数量的线程池(优先级)
     *
     * @param name     线程名称前缀
     * @param nThreads 线程数量
     * @return 线程池
     */
    public static ExecutorService newPriorityThreadPool(String name, int nThreads) {
        ThreadFactory threadNormalFactory = newThreadFactory(name);
        PriorityBlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>(64, COMPARATOR);
        ExecutorService executorService = new ThreadPoolExecutor(nThreads, nThreads, 0L,
                                                                 TimeUnit.MILLISECONDS,
                                                                 workQueue, threadNormalFactory);
        register(executorService, name);
        return executorService;
    }

    /**
     * 创建固定线程数量的轮询线程池
     *
     * @param name 名称
     * @param i    线程数量
     * @return 线程池
     */
    public static ScheduledExecutorService newScheduledThreadPool(String name, int i) {
        ThreadFactory threadNormalFactory = newThreadFactory(name);
        ScheduledExecutorService scheduledThreadPool =
                Executors.newScheduledThreadPool(i, threadNormalFactory);
        register(scheduledThreadPool, name);
        return scheduledThreadPool;
    }

    private static void register(ExecutorService executorService, String name) {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) executorService;
            Metrics.REGISTRY.register(name(DaemonExecutors.class, name, "activeCount"),
                                      (Gauge<Integer>) () -> poolExecutor.getActiveCount());
            Metrics.REGISTRY.register(name(DaemonExecutors.class, name, "queuedTaskCount"),
                                      (Gauge<Long>) () -> ThreadPoolStatus
                                              .getQueuedTaskCount(poolExecutor));
            ThreadPoolStatus threadPoolStatus = new ThreadPoolStatus(poolExecutor);
            try {
                ObjectName objectName = NAME_FACTORY.createName(TYPE, DOMAIN, name);
                M_BEAN_SERVER.registerMBean(threadPoolStatus, objectName);
                LOGGER.debug("registered {}", objectName);
            } catch (InstanceAlreadyExistsException | NotCompliantMBeanException | MBeanRegistrationException e) {
                LOGGER.warn("register fail", e);
            }
        }
    }

    public static PriorityRunnable priority(Runnable runnable, int priority) {
        return new PriorityRunnable(runnable, priority);
    }


    public static class PriorityRunnable implements Runnable {

        int priority;
        Runnable runnable;

        PriorityRunnable(Runnable runnable, int priority) {
            this.priority = priority;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }
}
