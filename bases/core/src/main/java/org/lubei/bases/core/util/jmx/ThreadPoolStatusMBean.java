package org.lubei.bases.core.util.jmx;

/**
 * 线程池MBean接口
 */
public interface ThreadPoolStatusMBean {


    int getActiveCount();

    long getCompletedTaskCount();

    int getCorePoolSize();

    int getLargestPoolSize();

    int getMaximumPoolSize();

    int getPoolSize();

    long getTaskCount();

    long getQueuedTaskCount();
}
