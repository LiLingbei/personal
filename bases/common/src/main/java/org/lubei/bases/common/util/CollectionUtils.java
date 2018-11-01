package org.lubei.bases.common.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableCollection;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合工具类
 *
 * @author LiLingbei
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class CollectionUtils {

    /**
     * 判断集合是否为null或空
     *
     * @param collection 集合对象
     * @return true当集合为null或空，反之false
     */
    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 转换对象集合（如获取实体中属性），结果去null去重
     *
     * @param collection 对象集合
     * @param function   转换方法
     * @param <T>        参数泛型
     * @param <R>        结果泛型
     * @return 结果列表
     */
    public static <T, R> List<R> toUniqueList(Collection<T> collection, Function<T, R> function) {
        return collection.stream().filter(Objects::nonNull).map(function)
                .filter(Objects::nonNull).distinct()
                .collect(Collectors.toList());
    }

    /**
     * 转换对象集合为字符串数组
     *
     * @param collection 对象集合
     * @param fun        转换方法
     * @param <T>        参数泛型
     * @return 结果数组
     */
    public static <T> String[] toStringArray(Collection<T> collection, Function<T, String> fun) {
        return copyTo(collection, fun, new String[collection.size()]);
    }

    /**
     * 转换对象集合为整型数组
     *
     * @param collection 对象集合
     * @param fun        转换方法
     * @param <T>        参数泛型
     * @return 结果数组
     */
    public static <T> Integer[] toIntegerArray(Collection<T> collection, Function<T, Integer> fun) {
        return copyTo(collection, fun, new Integer[collection.size()]);
    }

    /**
     * 转换对象集合，以List返回
     *
     * @param collection 对象集合
     * @param function   转换方法
     * @param <T>        参数泛型
     * @param <R>        结果泛型
     * @return 结果列表
     */
    public static <T, R> List<R> transformList(Collection<T> collection, Function<T, R> function) {
        List<R> list = new ArrayList<>(collection.size());
        //noinspection Convert2streamapi
        for (T t : collection) {
            list.add(function.apply(t));
        }
        return list;
    }

    /**
     * 把满足条件的对象添加到集合中
     *
     * @param collection 集合
     * @param iterable   对象
     * @param predicate  条件
     * @param <T>        对象泛型
     */
    public static <T> void addIf(Collection<T> collection, Iterable<T> iterable,
                                 Predicate<T> predicate) {
        for (T t : iterable) {
            if (predicate.test(t)) {
                collection.add(t);
            }
        }
    }

    /**
     * 把Collection转换为ArrayList
     *
     * @param collection 集合
     * @param <T>        泛型
     * @return ArrayList
     */
    public static <T> ArrayList<T> castArrayList(Collection<T> collection) {
        int size = collection.size();
        if (collection instanceof ArrayList) {
            return (ArrayList<T>) collection;
        }
        ArrayList<T> list = new ArrayList<>(size);
        //noinspection UseBulkOperation
        collection.forEach(list::add);
        return list;
    }

    /**
     * 把多个集合中的元素整合到一个新建的{@link List}中
     *
     * @param collections 多个集合
     * @param <T>         集合元素泛型
     * @return 包含所有集合元素的一个新List
     */
    @SafeVarargs
    public static <T> ArrayList<T> addAllToNewList(Collection<T>... collections) {
        int size = sumAllSize(collections);
        ArrayList<T> list = new ArrayList<>(size);
        for (Collection<T> collection : collections) {
            if (collection != null) {
                //noinspection UseBulkOperation
                collection.forEach(list::add);
            }
        }
        return list;
    }

    /**
     * 把多个集合中的元素整合到一个新建的{@link Set}中
     *
     * @param collections 多个集合
     * @param <T>         集合元素泛型
     * @return 包含所有集合元素的一个新Set
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> addAllToNewSet(Collection<T>... collections) {
        int size = sumAllSize(collections);
        LinkedHashSet<T> set = Sets.newLinkedHashSetWithExpectedSize(size);
        for (Collection<T> collection : collections) {
            if (collection != null) {
                set.addAll(collection);
            }
        }
        return set;
    }

    /**
     * 把一个集合按照另一个集合排序并转换元素放到一个新List中
     *
     * @param origin   待排序集合
     * @param originBy 获取排序字段方法
     * @param order    参考集合
     * @param orderBy  获取参考字段方法
     * @param to       转换类型方法
     * @return 排序并转换类型后的新List
     */
    @SuppressWarnings("unchecked")
    public static <T, D, B, R> List<R> sortByTo(Collection<T> origin, Function<T, B> originBy,
                                                Collection<D> order, Function<D, B> orderBy,
                                                Function<T, R> to) {
        int size = origin.size();
        if (size == 0) {
            return Collections.emptyList();
        }
        checkArgument(!(origin.iterator().next() instanceof Iterable), "元素不能为集合类型！");
        Map<B, Object> map = Maps.newLinkedHashMapWithExpectedSize(order.size());
        final Object placeholder = "占位";
        order.stream().map(orderBy).forEach(it -> map.putIfAbsent(it, placeholder));
        List<T> list = new ArrayList<>(size);
        for (T t : origin) {
            map.compute(originBy.apply(t), (k, v) -> {
                if (v == null) {
                    list.add(t);
                    return null;
                } else if (v == placeholder) {
                    return t;
                } else if (v instanceof ArrayList) {
                    ((ArrayList<T>) v).add(t);
                    return v;
                } else {
                    List<T> tmp = new ArrayList<>();
                    Collections.addAll(tmp, (T) v, t);
                    return tmp;
                }
            });
        }
        List<R> result = new ArrayList<>(size);
        for (Object v : map.values()) {
            if (v instanceof ArrayList) {
                ((ArrayList<T>) v).forEach(it -> result.add(to.apply(it)));
            } else if (v != placeholder) {
                result.add(to.apply((T) v));
            }
        }
        list.forEach(it -> result.add(to.apply(it)));
        return result;
    }

    /**
     * 判断集合中是否包含另一集合任一元素
     *
     * @param items 元素集合
     * @param any   另一集合
     * @param <E>   元素类型
     * @return true包含，反之false
     */
    public static <E> boolean containsAny(Collection<E> items, Iterable<E> any) {
        if (any instanceof Set) {
            return _containsAny((Set<E>) any, items);
        } else {
            return _containsAny(items, any);
        }
    }

    public static <E, B> void deduplicateSort(Collection<E> items, Function<E, B> by,
                                              Comparator<B> comparator) {

    }

    public static <E, B> Collection<E> distinctOrdered(Collection<E> items, Function<E, B> by,
                                                       Comparator<B> comparator) {

        Stream<E> stream;
        if (items == null || (stream = items.stream().filter(Objects::nonNull)).count() <= 0) {
            return Collections.emptyList();
        }
        Iterator<E> iterator = stream.iterator();
        E first = iterator.next();
        B key = by.apply(first);
        checkArgument(key instanceof Comparable || comparator != null,
                      "comparator can't be null !");
        TreeMap<B, E> treeMap = new TreeMap<>(comparator);
        treeMap.put(key, first);
        while (iterator.hasNext()) {
            E v = checkNotNull(iterator.next());
            B k = checkNotNull(by.apply(v));
            treeMap.putIfAbsent(k, v);
        }
        return unmodifiableCollection(treeMap.values());
    }


    @SafeVarargs
    static <T> int sumAllSize(Collection<T>... collections) {
        int sum = 0;
        for (Collection<T> c : collections) {
            if (c != null) {
                sum += c.size();
            }
        }
        return sum;
    }

    static <E> boolean _containsAny(Collection<E> items, Iterable<E> any) {
        for (E e : any) {
            if (items.contains(e)) {
                return true;
            }
        }
        return false;
    }

    static <T, R> R[] copyTo(Iterable<T> from, Function<T, R> fun, R[] to) {
        int i = 0;
        for (T t : from) {
            to[i++] = fun.apply(t);
        }
        return to;
    }

}
