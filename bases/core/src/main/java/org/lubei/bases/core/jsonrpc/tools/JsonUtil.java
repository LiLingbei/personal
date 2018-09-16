package org.lubei.bases.core.jsonrpc.tools;

import org.lubei.bases.core.jsonrpc.engine.Response;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by postgres on 2015/10/9.
 */
public class JsonUtil {

    public static final String NOT_VALID_JSON = "数据异常：数据应以为对象'{'或数组'['，当前第一个字节为{}";
    public static final Type RESPONSE_OBJECT_TYPE = responseOf(Object.class).getType();

    static <T> Type responseTypeOf(Type elementType) {
        return new TypeToken<Response<T>>() {
        }.where(new TypeParameter<T>() {
        }, (TypeToken<T>) TypeToken.of(elementType)).getType();
    }

    static <T> Type responseTypeOf(Class<T> elementType) {
        if (Object.class.equals(elementType)) {
            return RESPONSE_OBJECT_TYPE;
        }
        return responseOf(elementType).getType();
    }

    static <T> TypeToken<Response<T>> responseOf(Class<T> elementType) {
        return new TypeToken<Response<T>>() {
        }.where(new TypeParameter<T>() {
        }, elementType);
    }

}
