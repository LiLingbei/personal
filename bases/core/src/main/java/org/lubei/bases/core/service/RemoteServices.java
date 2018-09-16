package org.lubei.bases.core.service;

import org.lubei.bases.core.GlobalRes;
import org.lubei.bases.core.Server;
import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.exception.ErrorCode;
import org.lubei.bases.core.rpc.IRpcClient;
import org.lubei.bases.core.rpc.RpcConfig;

import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zlzhang 远程服务工具类，提供远程服务得注册、反注册、服务获取等功能。 Created by sany on 2015/5/13.
 */
public final class RemoteServices {

    static final Logger LOGGER = LoggerFactory.getLogger(RemoteServices.class);
    private static final Monitor monitor = new Monitor();
    private static final DistributedCenterService distributedCenterService;
    /**
     * . rpcClient对象
     */
    private static IRpcClient rpcClient;
    /**
     * . service的内存缓存
     */
    private static Table<String, Class, Object> serviceTable;

    static {
//        monitor.enter();
        LOGGER.error("RemoteService初始化...");
        try {
            String rpcClientClass = RpcConfig.INSTANCE.getRpcClientClass();
            rpcClient = (IRpcClient) Class.forName(rpcClientClass).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOGGER.error(e.getMessage());
            Throwables.propagate(e);
        } catch (Exception e) {
            LOGGER.error("RemoteService初始化失败", e);
        } finally {
//            monitor.leave();
        }
        LOGGER.error("RemoteService初始化成功...");
        distributedCenterService = new DistributedCenterService();
        serviceTable = distributedCenterService.getServiceTable();
        registerService(IDistributedCenterService.class, distributedCenterService);
    }

    /**
     * . 防止创建
     */
    private RemoteServices() {

    }

    /**
     * . 通过服务对象id和类型，定向获取一个指定的远程服务.找不到该对象则返回空
     *
     * @param serverId : 服务对象id
     * @param clazz    : 务对象接口类
     * @param <T>      : 服务对象接口类型
     * @return : 服务接口类对象
     */
    public static <T> T getService(final String serverId, final Class<?> clazz) {
        if (serviceTable.contains(serverId, clazz)) {
            return (T) serviceTable.get(serverId, clazz);
        }
        return (T) callRemote(serverId, clazz);
    }

    /**
     * . 通过服务对象id和类型和实现类id，定向获取一个指定的远程服务.找不到该对象则返回空 该方法为特殊用法，用于区分一个接口有多个实现类并注册到同一个server的情况。
     *
     * @param serverId          : 服务对象id
     * @param clazz             : 务对象接口类
     * @param instanceClassName :服务实现类类名
     * @param <T>               : 服务对象接口类型
     * @return : 服务接口类对象
     */
    public static <T> T getService(final String serverId, final Class<?> clazz,
                                   final String instanceClassName) {

        try {
            //本地缓存
            Class zClass = Class.forName(instanceClassName);
            if (serviceTable.contains(serverId, zClass)) {
                return (T) serviceTable.get(serverId, zClass);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.trace("类{}没有定义,这里忽略对其缓存，每次按id动态获取");
        }

        return (T) callRemote(serverId, clazz, instanceClassName);
    }

    /**
     * . 根据服务对象类型获取一个随机的远程服务
     *
     * @param clazz : 远程服务得接口类型
     * @param <T>   : IService 的子类接口
     * @return T    : 接口实例代理
     */
    public static <T> T getService(final Class<?> clazz) {
        //先取缓存
        if (serviceTable.containsColumn(clazz)) {
            String serverId = GlobalRes.getServerId();
            return (T) serviceTable.column(clazz).get(serverId);
        }
        //取不到远程获取,随机取得一个
        return callRemote(null, clazz);
    }

    private static <T> T callRemote(final String serverId, final Class<?> clazz, String... args) {
        try {
            if (serverId == null) {
                T instanceProxy = rpcClient.getRemoteService((Class<T>) clazz);
                return instanceProxy;
            } else {
                T instanceProxy = rpcClient.getRemoteService(serverId, (Class<T>) clazz, args);
                return instanceProxy;
            }
        } catch (Exception e) {
            LOGGER.error("远程服务{}获取失败", clazz, e);
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
    }


    /**
     * 注册远程服务. 同一个服务无需注册多次； 统一类型服务不允许注册多次(通过服务内部处理线程实现)
     *
     * @param clazz   :反注册的服务接口类型
     * @param service :反注册服务实例
     * @param <S>     :反注册服务的接口类型
     */
    public static <S> void registerService(final Class<?> clazz, final S service) {
        LOGGER.debug("registerService 接口:{}, 实例:{}", clazz, service);
        if (clazz == null || service == null) {
            throw new BusinessException(ErrorCode.SERVICE_REGISTER_CONFLIT, "服务注册接口或服务实例为空，不允许注册");
        }
        //默认为当前server下的服务
        String serverId = GlobalRes.getServerId();

        if (serviceTable.contains(serverId, clazz)) {
            Object catchedService = serviceTable.get(serverId, clazz); //判断是否一个服务类型多个注册，如果重复注册则抛出异常。
            if (!service.getClass().equals(catchedService.getClass())) {
                // 先注册的为主clazz 注册实例不变，额外注册新的缓存将实现类进行记录
                serviceTable.put(serverId, service.getClass(), service);
                serviceTable.put(serverId, catchedService.getClass(), catchedService);
            } else if (!service.equals(catchedService)) {
                throw new BusinessException(ErrorCode.SERVICE_REGISTER_CONFLIT);
            } else {
                LOGGER.info("服务{}重复注册对象", clazz.getName());
                return;
            }
        } else        // 加入缓存
        {
            serviceTable.put(serverId, clazz, service);
        }

        try {
            rpcClient.registerRemoteService(clazz, service);
        } catch (Exception e) {
            LOGGER.error("注册远程服务{}失败", service, e);
            throw new BusinessException(ErrorCode.SERVICE_NOT_FOUND);
        }
    }

    /**
     * . 反注册远程服务
     *
     * @param clazz   :反注册的服务接口类型
     * @param service :反注册服务实例
     * @param <S>     :反注册服务的接口类型
     */
    public static <S> void unregisterService(final Class<?> clazz, final S service) {
        String serverId = GlobalRes.getServerId();
        if (serviceTable.contains(serverId, clazz)) {
            serviceTable.remove(serverId, clazz);
            rpcClient.unregisterRemoteService(clazz, service);
        }
    }

    public static Collection<IService> listOnlineServices() {
        return null;// (Collection<IService>) serviceMap.values();
    }

    /**
     * 获取远程服务所在的server列表
     *
     * @param clazz 服务接口
     * @return server列表
     */
    public static List<Server> getServers(Class<?> clazz) {
        return rpcClient.getServers(clazz);
    }

    /**
     * 判断远程服务class是否已经存在
     *
     * @param clazz 服务接口
     * @return 存在返回true, 反之false
     */
    public static boolean contains(Class<?> clazz) {
        if (serviceTable.containsColumn(clazz)) {
            return true;
        }
        //远程获取一次
        List<Server> servers = getServers(clazz);
        return servers.isEmpty();
    }

    public static Map<String, String> getServices(Class<?> clazz) {
        return distributedCenterService.getDistributedService(clazz);
    }
}
