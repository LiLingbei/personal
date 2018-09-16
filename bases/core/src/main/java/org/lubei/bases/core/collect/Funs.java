package org.lubei.bases.core.collect;

/**
 * Function工具类
 *
 * @author LiLingbei
 */
public final class Funs {

    /**
     * 返回对象自身的方法
     *
     * @param t   对象
     * @param <T> 泛型
     * @return 对象自身
     */
    public static <T> T self(T t) {
        return t;
    }

    private Funs() {
        throw new IllegalAccessError();
    }
}
