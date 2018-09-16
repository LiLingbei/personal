package org.lubei.bases.core.service;


/**
 * 通用服务接口<br>
 *
 * @author panhongliang
 */
public interface IService {

    default String getServiceName() {
        return this.getClass().getName();
    }

    default String getServiceId() {
        //TODO 实现存在问题，无法百分百保证id唯一
        return this.getClass().getName() + ":" + this.hashCode();
    }

    default boolean isAvaliable() {
        return true;
    }
}
