package org.lubei.bases.core.service;

import org.lubei.bases.core.db.Db;
import org.lubei.bases.core.rpc.IRpcClient;
import org.lubei.bases.core.service.loader.ServiceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Services工具类，类似机房中的 ServiceUtils <br> 调用方法1：ITestTreeService service =
 * Services.getService(ITestTreeService.class,db);<br> service.deleteById("test");<br>
 * 调用方法2：ITestTreeService service = Services.getService(ITestTreeService.class);
 * service.setDb(DB);<br> service.deleteById("test");<br> 调用方法3：<br> GlobalRes.setDefaultDd(DB);<br>
 * ITestTreeService service = Services.getService(ITestTreeService.class); <br>
 * service.deleteById("test");<br> <br> 配置方式如下：【自定义配置优先 】<br> 1.零配置方式，采用默认规则<br>
 * 如：符合系统标准包结构，实现类在服务接口包.impl下，并且实现类名称就是接口名称，去掉前缀"I"<br> 如： 此接口 com.its.itone.core.service.ITestService<br>
 * 对应实现类 com.its.itone.core.service.impl.TestService<br> <br> 2.自定义配置方法：
 * META-INF/services/目录里同时创建一个以服务接口命名的文件;该文件里就是实现该服务接口的具体实现类 <br> 如：文件
 * META-INF/services/com.its.itone.core.service.ITestService <br> 内容是
 * com.its.itone.core.service.impl.TestService<br>
 *
 * 参考测试用例 com.its.itone.core.service.TreeServicesTest <br> TODO 目前不支持事务<br> 事务在方法内部自己实现
 *
 * @author panhongliang<br>
 */
@Deprecated
public final class Services {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Services.class);
    @SuppressWarnings("rawtypes")
    private static ServiceManager serviceManager = new ServiceManager();
    private static IRpcClient rpcClient;


    /**
     * Constructors.
     */
    private Services() {
    }

    /**
     * 获取通用服务 getService.
     *
     * @param <S>   extends IService
     * @param clazz Class
     * @return <S extends IService>
     */
    @SuppressWarnings("unchecked")
    public static <S extends IService> S getService(final Class<?> clazz) {
        return (S) serviceManager.getService(clazz);
    }

    /**
     * 获取数据库服务
     *
     * @return <S extends IBaseService>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S extends IBaseService> S getService(final Class<?> clazz, final Db db) {
        S service = (S) serviceManager.getService(clazz);
        service.setDb(db);
        return service;
    }

    /**
     * @return serviceManager
     */
    @SuppressWarnings("rawtypes")
    public static final ServiceManager getServiceManager() {
        return serviceManager;
    }
/*
    @SuppressWarnings("unchecked")
    public static <S extends IService> void registerService(final Class<S> clazz, final S service) {
        serviceManager.registerService(clazz, service);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IService> T getRemoteService(final RpcServerParams server,
                                                          final Class<?> serviceClass) {
        LOGGER.debug("Rpc call service {} on {} !", serviceClass, server);
        try {
            return rpcClient.getRemoteService(server, serviceClass);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            Throwables.propagate(e);
            return null;
        }
    }
    */
}
