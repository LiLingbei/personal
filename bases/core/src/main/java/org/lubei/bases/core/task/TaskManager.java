package org.lubei.bases.core.task;

import com.alibaba.fastjson.JSON;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 任务管理器
 */
public class TaskManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);
    private final Map<String, Service> tasks;

    public TaskManager() {
        tasks = Maps.newConcurrentMap();
    }

    /**
     * 注册并启动服务
     * 
     * @param register 注册者
     * @param id 唯一标识
     * @param service 服务
     * @return 服务（原样返回）
     */
    public synchronized Service startService(final String register, final String id,
                                             final Service service) {
        LOGGER.info("startService {} {} {}", id, register, service);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(register));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkNotNull(service);
        Preconditions.checkArgument(!tasks.containsKey(id), "already registered {}", id);
        tasks.put(id, service);
        service.startAsync();
        return service;
    }

    /**
     * 通过类名启动服务
     * 
     * @param register 注册者
     * @param id 唯一标识
     * @param serviceClass 服务类名称（该类必须为guava.Service）
     * @return 启动后的服务
     */
    public synchronized Service startService(final String register, final String id,
                                             final String serviceClass) {
        try {
            Class<?> clazz = Class.forName(serviceClass);
            Object instance = clazz.newInstance();
            Service service = (Service) instance;
            startService(register, id, service);
            return service;
        } catch (Throwable t) {
            LOGGER.error("startService fail: {}", id, t);
            throw new IllegalStateException(t);
        }
    }

    /**
     * 批量启动服务
     * 
     * @param register 注册者
     * @param confContent 配置信息（JSON字符串，ServiceEntry的列表）
     */
    public synchronized void startServices(final String register, final String confContent) {
        List<TaskEntry> services = JSON.parseArray(confContent, TaskEntry.class);
        for (TaskEntry se : services) {
            try {
                startService(register, se.id, se.className);
            } catch (Throwable t) {
                LOGGER.error("start service [{}] fail:", se.id, t);
            }
        }
    }

    /**
     * 停止所有服务 stop. String
     */
    public synchronized void stop() {
        for (Service service : tasks.values()) {
            service.stopAsync();
        }
    }

    @Override
    public String toString() {
        ToStringHelper s = MoreObjects.toStringHelper(this);
        for (Entry<String, Service> ent : tasks.entrySet()) {
            s.add(ent.getKey(), ent.getValue());
        }
        return s.toString();
    }
}
