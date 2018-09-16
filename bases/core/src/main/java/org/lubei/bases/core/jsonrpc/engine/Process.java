package org.lubei.bases.core.jsonrpc.engine;

/**
 * 支持抛出异常的Function
 */
@FunctionalInterface
public interface Process<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws Exception;
}
