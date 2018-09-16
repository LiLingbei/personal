package org.lubei.bases.core.rpc.zbus;

import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.Server;
import org.lubei.bases.core.rpc.IRpcClient;
import org.lubei.bases.core.rpc.RpcServerParams;
import org.lubei.bases.core.util.ToJsonWrapper;
import org.lubei.bases.core.util.ZBusUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zbus.broker.Broker;
import org.zbus.rpc.RpcConfig;
import org.zbus.rpc.RpcFactory;
import org.zbus.rpc.RpcProcessor;
import org.zbus.rpc.mq.MqInvoker;
import org.zbus.rpc.mq.Service;
import org.zbus.rpc.mq.ServiceConfig;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


/**
 * zubs客户端 <p> <p> 每个单独的client启动一个broker，支持两个通道：个人通道(self_服务器)，广播通道(broadcast)。 <p> 每个通道支持的服务是一致的。
 */
public class ZBusClient implements IRpcClient {

    public static final String BROAD_CAST = "broadCast";
    public static final String SELF = "self_";

    private static Logger LOGGER = LoggerFactory.getLogger(ZBusClient.class);
    /**
     * 远端rpc工厂的缓存
     */
    final Cache<String, RpcFactory> remoteFactoryCache;
    final Cache<String, Object> serviceCache;
    private final Broker broker;
    private final RpcProcessor rpcProcessor;

    private final Set<String> localService;
    private final String serverId;
    private final PingPong pingPong;

    public ZBusClient() {
        this(GlobalRes.getServer());
    }

    // 为了方便单元测试,增加protected 构造函数
    protected ZBusClient(Server server) {
        this.broker = ZBusUtil.getInstance().getBroker();
        this.serverId = server.getId();
        rpcProcessor = new RpcProcessor();
        startRpcService("个人", 10, SELF + serverId);

        serviceCache = CacheBuilder.newBuilder().build();
        remoteFactoryCache = CacheBuilder.newBuilder().build(
                new CacheLoader<String, RpcFactory>() {
                    @Override
                    public RpcFactory load(String key) throws Exception {
                        MqInvoker mqInvoker = new MqInvoker(broker, SELF + key);
                        return new RpcFactory(mqInvoker);
                    }
                });
        this.localService = Sets.newConcurrentHashSet();
        this.pingPong = new PingPong(broker, BROAD_CAST, localService, server);
    }


    private String name(Class clazz) {
        return clazz.getName();
    }

    private void startRpcService(String name, int consumerCount, String mq) {
        ServiceConfig config = new ServiceConfig();
        config.setConsumerCount(consumerCount);
        config.setMq(mq);
        config.setBroker(broker);
        config.setMessageProcessor(rpcProcessor);
        LOGGER.info("启动{}, 配置:{}", name, ToJsonWrapper.wrap(config));
        Service svc = new Service(config);
        try {
            svc.start();
        } catch (IOException e) {
            throw new IllegalStateException("启动失败", e);
        }
    }

    @Override
    public List<Server> getServers(Class clazz) {
        return pingPong.findServers(name(clazz));
    }

    @Override
    public <T> T getRemoteService(RpcServerParams server, Class<?> serviceClass) {
        return null;
    }

    @Override
    public <T> T getRemoteService(Class<T> serviceClass) {
        String name = name(serviceClass);
        String remoteId = pingPong.findServer(name);
        return getRemoteService(remoteId, serviceClass);
    }


    public Object safeGetService(RpcFactory factory, Class clazz, String name) {
        try {
            RpcConfig rpcConfig = new RpcConfig();
//            rpcConfig.setModule(clazz.getName());
            rpcConfig.setModule(name);
            rpcConfig.setTimeout(ZBusUtil.getInstance().getRpcTimeOut());
            return factory.getService(clazz, rpcConfig);
        } catch (Exception e) {
            throw new IllegalStateException("获取服务失败" + e.getMessage());
        }
    }

    private RpcFactory newFactory(String serviceId) {
        return new RpcFactory(new MqInvoker(broker, SELF + serviceId));
    }

    private Object _getRemoteService(String serviceId, Class clazz, String name) {
        RpcFactory factory;
        try {
            factory = remoteFactoryCache.get(serviceId, () -> newFactory(serviceId));
        } catch (ExecutionException e) {
            throw new IllegalStateException("不可能发生", e);
        }
        return safeGetService(factory, clazz, name);
    }

    @Override
    public <T> T getRemoteService(String serviceId, Class<T> serviceClass) {
        String interfaceName = name(serviceClass);
        String name = serviceId + ":" + interfaceName;

        try {
            Callable callable = () -> _getRemoteService(serviceId, serviceClass, interfaceName);
            return (T) serviceCache.get(name, callable);
        } catch (ExecutionException e) {
            throw new IllegalStateException("", e);
        }
    }

    @Override
    public <T> T getRemoteService(String serverId, Class<T> serviceInterface,
                                  String... args) {
        if (args.length == 0) {
            return getRemoteService(serverId, serviceInterface);
        }

        String instanceId = args[0];
        String name = serverId + ":" + name(serviceInterface) + ":" + instanceId;
        try {
            return (T) serviceCache.get(name, () -> this._getRemoteService(
                    serverId, serviceInterface, instanceId));
        } catch (ExecutionException e) {
            throw new IllegalStateException("", e);
        }
    }

    @Override
    public <T> void registerRemoteService(Class<?> serviceClass, T service) {
        String name = name(serviceClass); //注册接口
        rpcProcessor.addModule(name, service);
        localService.add(name);
        name = name(service.getClass()); //注册实例
        rpcProcessor.addModule(name, service);
        // 通知更新远端服务
        localService.add(name);
        pingPong.sendPong();
    }

    @Override
    public <T> void unregisterRemoteService(Class<?> serviceClass, T service) {
    }

    public void stop() {

    }

}

