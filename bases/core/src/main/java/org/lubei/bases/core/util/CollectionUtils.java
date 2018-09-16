package org.lubei.bases.core.util;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @deprecated 该代码未被谨慎检查，不建议使用（在1.2版本将删除）
 */
@Deprecated
public class CollectionUtils {

    /**
     * map转对象方法<br> mapToObject(map, TopoView.class)<br> TopoView topo =
     * JSON.parseObject(JSON.toJSONString(map), TopoView.class);
     *
     * @return T
     */
    @SuppressWarnings("rawtypes")
    public static final <T> T mapToObject(final Map map, final Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    /**
     * List<String>转List<Integer> .
     *
     * @return List<Integer>
     */
    public static List<Integer> stringsToIntegers(final List<String> datas) {
        return Lists.transform(datas, new Function<String, Integer>() {
            @Override
            public Integer apply(final String entry) {
                Preconditions.checkArgument(null != entry);
                return Integer.parseInt(entry);
            }
        });
    }

    /**
     * List<String>转List<Long> .
     *
     * @return List<Long>
     */
    public static List<Long> stringsToLongs(final List<String> datas) {
        return Lists.transform(datas, new Function<String, Long>() {
            @Override
            public Long apply(final String entry) {
                return Long.parseLong(entry);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(final Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(final Collection coll) {
        return !CollectionUtils.isEmpty(coll);
    }
}
