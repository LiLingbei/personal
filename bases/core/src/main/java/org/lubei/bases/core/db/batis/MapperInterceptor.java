package org.lubei.bases.core.db.batis;

import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.util.Properties;

/**
 * 通用Mapper拦截器
 *
 * @author liwenheng
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                                                                    Object.class, RowBounds.class,
                                                                    ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                                                                     Object.class})
})
public class MapperInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapperInterceptor.class);
    private final MapperHelper mapperHelper = new MapperHelper();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] objects = invocation.getArgs();
        MappedStatement ms = (MappedStatement) objects[0];
        String msId = ms.getId();
        //不需要拦截的方法直接返回
        if (mapperHelper.isMapperMethod(msId)) {
            //第一次经过处理后，就不会是ProviderSqlSource了，一开始高并发时可能会执行多次，但不影响。以后就不会在执行了
            if (ms.getSqlSource() instanceof ProviderSqlSource) {
                LOGGER.trace("intercept {}", msId);
                mapperHelper.setSqlSource(ms);
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {
        mapperHelper.setProperties(properties);
    }
}
