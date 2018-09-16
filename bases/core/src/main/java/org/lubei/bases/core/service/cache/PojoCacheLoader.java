package org.lubei.bases.core.service.cache;

import org.lubei.bases.core.service.IBaseService;
import org.lubei.bases.core.service.pojo.BasePojo;

import com.google.common.cache.CacheLoader;

@SuppressWarnings("rawtypes")
@Deprecated
public class PojoCacheLoader<T extends BasePojo, K, S extends IBaseService<T, K>> extends
                                                                                  CacheLoader<K, T> {
    private S service;

    @Override
    public T load(final K key) throws Exception {
        return service.getById(key);
    }

    /**
     * @return service -service
     */
    public final S getService() {
        return service;
    }

    /**
     * @param service - service.
     */
    public final void setService(final S service) {
        this.service = service;
    }
}
