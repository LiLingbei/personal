package org.lubei.bases.core.task;

import com.google.common.util.concurrent.AbstractScheduledService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 周期任务<br> 如：定期汇总数据
 *
 * @author panhongliang
 */
public abstract class ScheduledTask extends AbstractScheduledService {

    @Override
    protected ScheduledExecutorService executor() {
        String name = serviceName();
        return Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name));
    }
}
