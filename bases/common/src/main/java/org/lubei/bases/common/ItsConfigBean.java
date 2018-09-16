package org.lubei.bases.common;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Map;

/**
 * its总配置文件，包括部署模式、RPC配置、启动服务等配置信息
 */
public class ItsConfigBean {
    /**
     * server Id
     */
    // private int serverId;
    /**
     * 是否为单一部署
     */
    private boolean single;
    /**
     * 是否启用自身监控
     */
    private boolean isSelfMonitoring;
    /**
     * 是否确认资产
     */
    private boolean isConfirmed;
    /**
     * RPC配置字符串
     */
    private String rpc;
    /**
     * RPC配置字符串
     */
    private String rpcServerClass;
    /**
     * RPC配置字符串
     */
    private String rpcClientClass;
    /**
     * 所需启动的服务类列表
     */
    private List<String> daemons;

    /**
     * 配置变化同步周期，单位秒
     */
    private int configChangeSyncPeriod;
    /**
     * 额外参数，供后期扩展用
     */
    private Map<String, Object> params;

    public ItsConfigBean() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ItsConfigBean.class).add("single", single)
                .add("rpc", rpc).add("daemons", daemons)
                .add("copyProperties", configChangeSyncPeriod)
                .add("params", params).toString();
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }


    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public List<String> getDaemons() {
        return daemons;
    }

    public void setDaemons(List<String> daemons) {
        this.daemons = daemons;
    }

    public int getConfigChangeSyncPeriod() {
        return configChangeSyncPeriod;
    }

    public void setConfigChangeSyncPeriod(int configChangeSyncPeriod) {
        this.configChangeSyncPeriod = configChangeSyncPeriod;
    }

    public void copyTo(ItsConfigBean instance) {
        instance.single = this.single;
        instance.rpc = this.rpc;
        instance.configChangeSyncPeriod = this.configChangeSyncPeriod;
        instance.daemons = this.daemons;
        instance.isSelfMonitoring = this.isSelfMonitoring;
        if (instance.params != null) {
            instance.params.putAll(this.params);
        }
    }

    public boolean isSelfMonitoring() {
        return isSelfMonitoring;
    }

    public void setIsSelfMonitoring(boolean isSelfMonitoring) {
        this.isSelfMonitoring = isSelfMonitoring;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void setIsConfirmed(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    public String getRpcServerClass() {
        return rpcServerClass;
    }

    public void setRpcServerClass(String rpcServerClass) {
        this.rpcServerClass = rpcServerClass;
    }

    public String getRpcClientClass() {
        return rpcClientClass;
    }

    public void setRpcClientClass(String rpcClientClass) {
        this.rpcClientClass = rpcClientClass;
    }
}
