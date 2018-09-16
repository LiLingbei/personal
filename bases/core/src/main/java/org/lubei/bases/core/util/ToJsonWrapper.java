package org.lubei.bases.core.util;

import com.alibaba.fastjson.JSON;

/**
 * JSON字符串包装器，toString方法返回被包装对象的JSON字符串，一般用来输出日志
 */
public class ToJsonWrapper {

    private Object object;

    public static ToJsonWrapper wrap(Object object) {
        return new ToJsonWrapper(object);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(object);
    }

    public ToJsonWrapper(Object object) {
        this.object = object;
    }


    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }


}
