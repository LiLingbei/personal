package org.lubei.bases.core.rpc;

/**
 * rpc连接配置参数
 */
public enum RpcConfig {
    INSTANCE;
    /**
     * RPC配置字符串
     */
    private String rpcUrl;
    /**
     * RPC配置字符串
     */
    private String rpcServerClass;
    /**
     * RPC配置字符串
     */
    private String rpcClientClass="org.lubei.bases.core.rpc.zbus.ZBusClient";

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
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
