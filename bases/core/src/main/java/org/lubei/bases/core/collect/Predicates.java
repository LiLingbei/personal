package org.lubei.bases.core.collect;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 判定条件工具类
 *
 * @author LiLingbei
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Predicates {

    private static final Predicate NOTNULL = Objects::nonNull;

    /**
     * 获取非空条件
     *
     * @param <T> 对象泛型
     * @return 非空条件
     * @deprecated 可以使用Objects::nonNull
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Predicate<T> notNull() {
        return NOTNULL;
    }

}
