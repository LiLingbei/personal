package org.lubei.bases.core.util.jmx;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池Mbean实现
 */
public class ThreadPoolStatus implements ThreadPoolStatusMBean {

    private ThreadPoolExecutor executor;

    public ThreadPoolStatus(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public static long getQueuedTaskCount(ThreadPoolExecutor executor) {
        return executor.getTaskCount() - executor.getCompletedTaskCount() - executor
                .getActiveCount();
    }

    @Override
    public int getActiveCount() {
        return executor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    @Override
    public int getCorePoolSize() {
        return executor.getCorePoolSize();
    }

    @Override
    public int getLargestPoolSize() {
        return executor.getLargestPoolSize();
    }

    @Override
    public int getMaximumPoolSize() {
        return executor.getMaximumPoolSize();
    }

    @Override
    public int getPoolSize() {
        return executor.getPoolSize();
    }

    @Override
    public long getTaskCount() {
        return executor.getTaskCount();
    }

    @Override
    public long getQueuedTaskCount() {
        return getQueuedTaskCount(executor);
    }
}
