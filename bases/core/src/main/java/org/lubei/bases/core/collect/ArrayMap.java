package org.lubei.bases.core.collect;

import com.google.common.collect.ImmutableSet;

import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;

/**
 * 基于数组的Map
 */
public class ArrayMap<K, V> extends AbstractMap<K, V> {

    private final K[] labels;
    private final V[] row;

    /**
     * 构造
     *
     * @param labels 名称数组
     * @param row    值数组
     */
    public ArrayMap(K[] labels, V[] row) {
        this.labels = labels;
        this.row = row;
    }

    @Override
    public int size() {
        return row.length;
    }

    @Override
    public boolean containsValue(Object value) {
        for (V v : row) {
            if (Objects.equals(v, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        //noinspection unchecked
        return index((K) key) >= 0;
    }

    @Override
    public V get(Object key) {
        if (key instanceof Integer) {
            return get(((Integer) key).intValue());
        }
        @SuppressWarnings("unchecked") int id = index((K) key);
        if (id < 0) {
            return null;
        }
        return row[id];
    }

    @Override
    public V put(K key, V value) {
        int id = index(key);
        if (id < 0) {
            return null;
        }
        row[id] = value;
        return value;
    }

    @Override
    public V remove(Object key) {
        throw new IllegalAccessError("not support remove");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        ImmutableSet.Builder<Entry<K, V>> builder = ImmutableSet.builder();
        for (int i = 0; i < labels.length; i++) {
            builder.add(new IdEntry(i));
        }

        return builder.build();
    }

    /**
     * 判断是否包含某个值（兼容LIST）
     *
     * @param value 查找值
     * @return 是否包含
     */
    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * 获取第几个值（兼容LIST）
     *
     * @param index 位置
     * @return 值
     */
    public V get(int index) {
        return row[index];
    }

    /**
     * 设置第几个值（兼容LIST）
     *
     * @param index   位置
     * @param element 新值
     * @return 旧值
     */
    public V set(int index, V element) {
        V old = row[index];
        row[index] = element;
        return old;
    }

    private int index(K key) {
        for (int i = 0; i < labels.length; i++) {
            if (Objects.equals(key, labels[i])) {
                return i;
            }
        }
        return -1;
    }

    class IdEntry implements Entry<K, V> {

        final int id;

        public IdEntry(int id) {
            this.id = id;
        }

        @Override
        public K getKey() {
            return labels[id];
        }

        @Override
        public V getValue() {
            return row[id];
        }

        @Override
        public V setValue(V value) {
            return row[id] = value;
        }
    }
}
