package org.lubei.bases.core.util;

import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * 性能指标收集
 */
public class Metrics {

    /**
     * 指标注册表
     */
    public static final MetricRegistry REGISTRY = new MetricRegistry();
    /**
     * 检查注册表
     */
    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();
    static final Logger LOGGER = LoggerFactory.getLogger(Metrics.class);

    public static void initBasicMetrics() {
        REGISTRY.register("jvm.attribute", new JvmAttributeGaugeSet());
        REGISTRY.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory
                                                                         .getPlatformMBeanServer()));
        REGISTRY.register("jvm.classloader", new ClassLoadingGaugeSet());
        REGISTRY.register("jvm.filedescriptor", new FileDescriptorRatioGauge());
        REGISTRY.register("jvm.gc", new GarbageCollectorMetricSet());
        REGISTRY.register("jvm.memory", new MemoryUsageGaugeSet());
        REGISTRY.register("jvm.threads", new ThreadStatesGaugeSet());
        LOGGER.info("initBasicMetrics ok, {}", REGISTRY);
    }

}
