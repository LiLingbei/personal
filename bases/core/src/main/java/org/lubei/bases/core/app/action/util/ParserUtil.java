package org.lubei.bases.core.app.action.util;

import com.alibaba.fastjson.JSON;
import org.mozilla.javascript.Undefined;

/**
 * @author sany Created by sany on 17-4-26.
 */
public class ParserUtil {

    public static Object to(Object arg, Class<?> type) {
        if (arg.getClass().isAssignableFrom(type)) {
            return arg;
        }
        if (arg.equals(Undefined.instance) || jdk.nashorn.internal.runtime.Undefined
                .getUndefined().equals(arg) || "undefined".equals(arg) || "null".equals(arg)) {
            return JSON.parseObject("{}", type);
        }
        String s = JSON.toJSONString(arg);
        return JSON.parseObject(s, type);
    }
}
