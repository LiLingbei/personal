package org.lubei.bases.core.rpc.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自定义线程池
 */
@Deprecated
public class ThreadPool extends AbstractExecutorService {

    private final Lock lock = new ReentrantLock();
    private final BlockingQueue<Runnable> workQueue;
    private final ExecutorService executor;
    private final String poolName;
    private final long maxTimeSliceMillis;
    private final BlockingQueue<Drainer> drainerList;

    private final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);

    /**
     * @param maxTimeSlice     - the maximum time slice of a drainer can run
     * @param maxTimeSliceUnit - the unit of the maxTimeSlice argument
     */
    public ThreadPool(
            BlockingQueue<Runnable> workQueue, ExecutorService executor,
            String poolName, int maxDrainers, long maxTimeSlice, TimeUnit maxTimeSliceUnit) {
        this.workQueue = workQueue;
        this.executor = executor;
        this.poolName = poolName;
        this.maxTimeSliceMillis = maxTimeSliceUnit.toMillis(maxTimeSlice);
        drainerList = new ArrayBlockingQueue<Drainer>(maxDrainers);

        for (int i = 0; i < maxDrainers; i++) {
            drainerList.add(new Drainer(String.format("%s-%03d", this.poolName, i)));
        }
    }

    public ThreadPool(
            BlockingQueue<Runnable> workQueue, ExecutorService executor,
            int maxDrainers, long maxTimeSlice, TimeUnit maxTimeSliceUnit) {
        this(workQueue, executor, "Drainer", maxDrainers, maxTimeSlice, maxTimeSliceUnit);
    }

    public ThreadPool(
            BlockingQueue<Runnable> workQueue, ExecutorService executor,
            String poolName, int maxDrainers) {
        this(workQueue, executor, poolName, maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public ThreadPool(
            BlockingQueue<Runnable> workQueue,
            ExecutorService executor,
            int maxDrainers) {
        this(workQueue, executor, "Drainer", maxDrainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public ThreadPool(
            ExecutorService executor, long maxTimeSlice, TimeUnit maxTimeSliceUnit) {
        this(new LinkedBlockingQueue<Runnable>(),
             executor, "Drainer", 1, maxTimeSlice, maxTimeSliceUnit);
    }

    public ThreadPool(ExecutorService executor) {
        this(new LinkedBlockingQueue<Runnable>(), executor, 1);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public synchronized List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTerminated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(Runnable task) {
        workQueue.offer(task);
        lock.lock();
        try {
            if (!drainerList.isEmpty()) {
                executor.execute(drainerList.poll());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 任务执行 Drainer
     */
    private class Drainer implements Runnable {

        private final String threadName;

        private Drainer(String threadName) {
            this.threadName = threadName;
        }

        public void run() {
            Thread thread = Thread.currentThread();
            String oldName = thread.getName();
            thread.setName(threadName);

            try {
                internalRun();
            } finally {
                thread.setName(oldName);
            }

        }

        private void internalRun() {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < maxTimeSliceMillis) {
                Runnable task = null;

                lock.lock();
                try {
                    task = workQueue.poll();
                    if (task == null) {
                        drainerList.add(this);
                        return;
                    }
                } finally {
                    lock.unlock();
                }

                try {
                    task.run();
                } catch (RuntimeException e) {
                    LOGGER.warn("Ignoring Task Failure", e);
                }
            }

            lock.lock();
            try {
                if (workQueue.isEmpty()) {
                    drainerList.add(this);
                } else {
                    executor.execute(this);
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
