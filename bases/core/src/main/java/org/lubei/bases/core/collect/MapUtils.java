package org.lubei.bases.core.collect;

import kotlin.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Map工具类
 *
 * @author LiLingbei
 */
public class MapUtils {

    private static final Pair<Boolean, ?> NONE = new Pair<>(false, null);


    /**
     * 获取Map中是否存在key以及对应的value
     *
     * @return Pair对象，1：是否存在；2：对应value。
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Pair<Boolean, V> getExist(Map<K, V> map, K key) {
        Object o = ((Map<K, Object>) map).getOrDefault(key, NONE);
        return o == NONE ? (Pair<Boolean, V>) NONE : new Pair<>(true, (V) o);
    }

    /**
     * 判断Map是否可编辑
     *
     * @param map Map
     * @return true可以，反之false
     */
    public static boolean isEditable(Map<String, Object> map) {
        try {
            map.put("测试键", null);
            map.remove("测试键");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断Map是否满足条件
     *
     * @param map  Map
     * @param cond 条件
     * @return true满足，反之false
     */
    public static <K, V> boolean match(Map<K, V> map, Map<K, V> cond) {
        return match(map, (cond == null || cond.isEmpty() ? null : cond.entrySet()));
    }

    /**
     * 判断Map是否满足条件
     *
     * @param maps Map集合
     * @param cond 条件
     * @return true满足，反之false
     */
    public static <K, V> List<Map<K, V>> filter(Iterable<Map<K, V>> maps, Map<K, V> cond) {
        if (maps == null || !maps.iterator().hasNext()) {
            return Collections.emptyList();
        }
        List<Map<K, V>> list = new ArrayList<>();
        Iterable<Map.Entry<K, V>> condList =
                cond == null || cond.isEmpty() ? null : cond.entrySet();
        for (Map<K, V> map : maps) {
            if (match(map, condList)) {
                list.add(map);
            }
        }
        return list;
    }

    private static <K, V> boolean match(Map<K, V> map, Iterable<Map.Entry<K, V>> condList) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        if (condList == null || !condList.iterator().hasNext()) {
            return true;
        }
        for (Map.Entry<K, V> entry : condList) {
            V val = map.get(entry.getKey());
            if (!entry.getValue().equals(val)) {
                return false;
            }
        }
        return true;
    }

}
