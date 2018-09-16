package org.lubei.bases.core.rpc;

import org.lubei.bases.core.rpc.utils.RpcConstants;
import org.lubei.bases.core.service.pojo.BasePojo4String;

import com.google.common.base.Strings;

/**
 * rpc服务端器参数
 *
 * 如：ip地址、端口、协议等
 *
 * rpc server params
 *
 * @author hongliangpan@gmail.com
 */
public class RpcServerParams extends BasePojo4String {

    private static final long serialVersionUID = -4743286871045075007L;

    /**
     * <code>host</code> - host .
     */
    private String host;

    /**
     * <code>port</code> - 端口 .
     */
    private String port;
    protected String protocol = RpcConstants.Protocol.Https.name();

    protected String serverType;

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public RpcServerParams() {
    }

    @Override
    public String toString() {
        return protocol + ":" + host + ":" + port;
    }

    public RpcServerParams(final String host, final String port) {
        this.host = host;
        this.port = port;
    }

    public String getServerId() {
        return getId();
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }


    public int getPortInt() {
        if (Strings.isNullOrEmpty(getPort())) {
            return 0;
        } else {
            return Integer.parseInt(getPort());
        }
    }

    public void setPort(final int port) {
        setPort(String.valueOf(port));
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        RpcServerParams server = (RpcServerParams) obj;
        return getHost().equals(server.getHost()) && getPortInt() == server.getPortInt();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getPortInt();
        hash = 31 * hash + (null == getHost() ? 0 : getHost().hashCode());
        return hash;
    }


    public String getUniqueKey() {
        return getServerId() + getHost() + getPort();
    }

    /**
     * @return host -host{return content description}
     */
    public final String getHost() {
        return host;
    }

    /**
     * @param host - host{parameter description}.
     */
    public final void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return port -port{return content description}
     */
    public final String getPort() {
        return port;
    }

    /**
     * @param port - port{parameter description}.
     */
    public final void setPort(final String port) {
        this.port = port;
    }


}
