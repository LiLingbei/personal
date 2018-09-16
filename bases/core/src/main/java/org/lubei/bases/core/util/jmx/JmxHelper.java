package org.lubei.bases.core.util.jmx;

import com.codahale.metrics.DefaultObjectNameFactory;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

/**
 * JMX工具类
 */
public class JmxHelper {

    /**
     * 统一的MBean服务
     */
    public static final MBeanServer M_BEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
    /**
     * 统一的名称工厂
     */
    public static final DefaultObjectNameFactory NAME_FACTORY = new DefaultObjectNameFactory();

}
