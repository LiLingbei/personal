package org.lubei.bases.core.collect;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 基于数组的ListMap，内存占用小
 */
public class Table<K, V> implements List<Map<K, V>> {


    List<V[]> rows = Lists.newArrayList();
    K[] labels;

    @SafeVarargs
    public Table(K... labels) {
        this.labels = labels;
    }

    /**
     * 构造方法，供序列化调用
     */
    @Deprecated
    Table() {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Table.class).add("labels", this.labels)
                .add("count", rows.size()).toString();
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Map<K, V>> iterator() {

        return new Iterator<Map<K, V>>() {
            int i;

            @Override
            public boolean hasNext() {
                return i < rows.size();
            }

            @Override
            public ArrayMap<K, V> next() {
                ArrayMap<K, V> map = new ArrayMap<>(labels, rows.get(i));
                i++;
                return map;
            }
        };
    }

    @Override
    public Object[] toArray() {
        throw new IllegalAccessError("not support");
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean add(Map<K, V> kvMap) {
        @SuppressWarnings("unchecked") V[] row = (V[]) new Object[labels.length];
        int i = 0;
        for (K label : labels) {
            row[i] = kvMap.get(label);
            i++;
        }
        rows.add(row);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean addAll(Collection<? extends Map<K, V>> c) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean addAll(int index, Collection<? extends Map<K, V>> c) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public void clear() {
        rows.clear();
    }

    @Override
    public Map<K, V> get(int index) {
        return new ArrayMap<>(labels, rows.get(index));
    }

    @Override
    public Map<K, V> set(int index, Map<K, V> element) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public void add(int index, Map<K, V> element) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public Map<K, V> remove(int index) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public int indexOf(Object o) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public ListIterator<Map<K, V>> listIterator() {
        throw new IllegalAccessError("not support");
    }

    @Override
    public ListIterator<Map<K, V>> listIterator(int index) {
        throw new IllegalAccessError("not support");
    }

    @Override
    public List<Map<K, V>> subList(int fromIndex, int toIndex) {
        throw new IllegalAccessError("not support");
    }


    @SafeVarargs
    public final boolean addRow(V... row) {
        Preconditions.checkArgument(row.length == labels.length);
        return rows.add(row);
    }

    /**
     * 供序列化用
     */
    @Deprecated
    public K[] getLabels() {
        return labels;
    }

    /**
     * 供序列化用
     */
    @Deprecated
    public void setLabels(K[] labels) {
        this.labels = labels;
    }

    /**
     * 供序列化用
     */
    @Deprecated
    public List<V[]> getRows() {
        return rows;
    }

    /**
     * 供序列化用
     */
    @Deprecated
    public void setRows(List<V[]> rows) {
        this.rows = rows;
    }


    /**
     * 转化为CSV字符串
     *
     * @return csv
     */
    public String asCsv() {
        StringBuilder stringBuilder = new StringBuilder();
        this.dumpAsCsv(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * 打印细节
     *
     * @param stringBuilder 缓存
     */
    public void dumpAsCsv(StringBuilder stringBuilder) {
        for (K label : labels) {
            stringBuilder.append(label).append('\t');
        }
        for (V[] row : rows) {
            stringBuilder.append('\n');
            for (V v : row) {
                if (v != null && v.getClass().isArray()) {
                    stringBuilder.append(Arrays.deepToString((Object[]) v)).append("\t");
                } else {
                    stringBuilder.append(v).append('\t');
                }
            }
        }
    }

    /**
     * 打印细节
     *
     * @param stringBuilder 缓存
     */
    public void dumpAsMdTable(StringBuilder stringBuilder) {
        if (labels == null || labels.length == 0) {
            return;
        }
        stringBuilder.append("\n\n|");
        for (K label : labels) {
            stringBuilder.append(label).append("|");
        }
        stringBuilder.append("\n|");
        for (K label : labels) {
            stringBuilder.append("---|");
        }

        for (V[] row : rows) {
            stringBuilder.append("\n|");
            for (V v : row) {
                Object val =
                        (v != null && v.getClass().isArray()) ? Arrays.deepToString((Object[]) v)
                                                              : v;
                stringBuilder.append(val).append('|');
            }
        }
        stringBuilder.append("\n");
    }
}
