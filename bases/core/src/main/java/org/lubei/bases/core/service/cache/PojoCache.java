package org.lubei.bases.core.service.cache;

import org.lubei.bases.core.service.IBaseService;
import org.lubei.bases.core.service.pojo.BasePojo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
@Deprecated
public class PojoCache<T extends BasePojo, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PojoCache.class);

    protected LoadingCache<K, T> loadingCache;

    public <S extends IBaseService<T, K>> void init(final S service, final Class clazz) {
        PojoCacheLoader<T, K, S> defLoader = new PojoCacheLoader<T, K, S>();
        defLoader.setService(service);
        loadingCache = CacheBuilder.newBuilder().build(defLoader);
        // GlobalRes.registerCache(service.getServiceId(), loadingCache);
        // GlobalRes.registerCache(clazz.getName(), loadingCache);
    }

    public void removeById(final K id) {
        loadingCache.invalidate(id);
    }

    public T getById(final K id) {
        try {
            return loadingCache.get(id);
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @return loadingCache -loadingCache{return content description}
     */
    public final LoadingCache<K, T> getLoadingCache() {
        return loadingCache;
    }

}
