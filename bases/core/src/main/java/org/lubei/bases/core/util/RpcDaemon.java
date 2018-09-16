package org.lubei.bases.core.util;

import org.lubei.bases.core.rpc.IRpcServer;
import org.lubei.bases.core.rpc.RpcConfig;
import org.lubei.bases.core.rpc.RpcServerParams;
import org.lubei.bases.core.rpc.utils.RpcConstants;
import org.lubei.bases.core.task.AbstractDaemon;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RPC服务
 */
public class RpcDaemon extends AbstractDaemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcDaemon.class);
    IRpcServer httpServer = null;
    String rpc;
    String rpcServerClass;
    RpcServerParams rpcServerParams;

    /**
     * 构造函数
     *
     * @param rpcConfig 参数字符串，格式必须为 http:ip:端口
     */
    public RpcDaemon(RpcConfig rpcConfig) {
        super();
        this.rpc = rpcConfig.getRpcUrl();
        this.rpcServerClass=rpcConfig.getRpcServerClass();
        this.rpcServerParams = getRpcServerParams(rpc);
    }

    /**
     * 从RPC字符串解析创建RpcServer对象
     *
     * @param rpc 参数字符串，格式必须为 http:ip:端口
     * @return RpcServer配置对象
     */
    public static RpcServerParams getRpcServerParams(String rpc) {
        Pattern pattern = Pattern.compile("https:((\\d{1,3}\\.){3}\\d{1,3}):(\\d+)");
        Matcher matcher = pattern.matcher(rpc);
        Preconditions.checkArgument(matcher.matches(), "格式必须为 https:ip:端口");
        MatchResult result = matcher.toMatchResult();
        String ip = result.group(1);
        String port = result.group(result.groupCount());
        RpcServerParams server = new RpcServerParams();
        server.setHost(ip);
        server.setPort(port);
        server.setProtocol(RpcConstants.Protocol.Https.name());
        return server;
    }

    /**
     * 获取本地server对象
     *
     * @return RpcServerParams
     */
    public RpcServerParams getLocalServerParams() {
        return rpcServerParams;
    }

    @Override
    protected void shutDown() throws Exception {
        if (null != httpServer) {
            httpServer.shutDown();
        }
    }

    @Override
    protected void startUp() throws Exception {
        try {
            LOGGER.info("启动rpc server {} by class{}",rpcServerParams,rpcServerClass);
            httpServer = (IRpcServer)Class.forName(rpcServerClass).newInstance();
            httpServer.init(this.rpcServerParams);
            httpServer.startUp();
            LOGGER.info("RPC 启动完毕");
        } catch (Throwable e) {
            if (e.getCause() instanceof BindException) {
                LOGGER.error("Address already in use{}!System maybe exit.", rpc, e);
                System.exit(1);
            }
            LOGGER.error("Rpc exception.", e);
        }
    }

}
