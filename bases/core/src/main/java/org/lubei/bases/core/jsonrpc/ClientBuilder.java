package org.lubei.bases.core.jsonrpc;

import org.lubei.bases.core.jsonrpc.tools.RpcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 用于调用jsonrpc的client的构建工具类.
 *
 * @author sany
 */
public final class ClientBuilder {

    /**
     * Logger.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(ClientBuilder.class);

    /**
     * 构造函数，防止实例化.
     */
    private ClientBuilder() {
    }

    /**
     * 根据token和rpc地址创建jsonrpc的客户端.
     *
     * @param url   :rpc地址
     * @param token :用户登陆时返回的token信息
     * @return RpcClient
     */
    public static RpcClient build(final String url, final String token) {
        TokenRpcClient tokenRpcClient = new TokenRpcClient(url);
        tokenRpcClient.setToken(token);
        return tokenRpcClient;
    }

    /**
     * 根据rpc地址和用户密码进行登陆，登陆成功返回token，失败抛出运行时异常.
     *
     * @param url      :rpc地址
     * @param user     :用户名
     * @param password :密码
     * @return 用户的登陆信息
     */
    public static String buildToken(final String url, final String user, final String password) {
        TokenRpcClient tokenRpcClient = new TokenRpcClient(url);
        tokenRpcClient.call("/login/login", user, password, true);
        return tokenRpcClient.getToken();
    }

    /**
     * 内部客户端代理，不对外提供.
     */
    private static class TokenRpcClient extends RpcClient {

        /**
         * 构造函数.
         *
         * @param rpcUrl :rpc地址
         */
        TokenRpcClient(final String rpcUrl) {
            super(rpcUrl);
        }

        /**
         * 获取token信息.
         *
         * @return ：返回用户登陆后的 token 信息，未登陆返回空
         */
        public String getToken() {
            Map header = (Map) this.getHttpClient().getDefaultHeaders();
            return (String) header.getOrDefault("Cookie", "");
        }

        /**
         * 设置token.
         *
         * @param token :设置登陆的token信息
         */
        public void setToken(final String token) {
            Map header = (Map) this.getHttpClient().getDefaultHeaders();
            header.put("Cookie", token);
        }

    }
}


