package org.lubei.bases.core.plugin.pojo;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 插件描述Map封装.
 *
 * @author gaojuhua.
 */
public class MapDescription extends Description implements Map<String, Object> {

    /**
     * 属性Map
     */
    private final Map<String, Object> attributes;

    /**
     * 构造函数
     */
    public MapDescription() {
        attributes = Maps.newHashMap();
    }

    /**
     * 构造函数
     *
     * @param attrs 属性Map
     */
    public MapDescription(Map<String, Object> attrs) {
        attributes = (attrs instanceof HashMap ? attrs : Maps.newHashMap(attrs));
    }

    /**
     * 获取属性Map
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }


    /**
     * 清除不该有的属性
     */
    public MapDescription purge() {
        return this;
    }

    /**
     * 添加属性
     *
     * @param name  属性名
     * @param value 属性值
     */
    @Override
    public Object put(String name, Object value) {
        return attributes.put(name, value);
    }

    /**
     * 添加多个属性
     *
     * @param attrs 属性Map
     */
    @Override
    public void putAll(Map<? extends String, ?> attrs) {
        attributes.putAll(attrs);
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return attributes.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return attributes.get(key);
    }

    @Override
    public Object remove(Object key) {
        return attributes.remove(key);
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public Collection<Object> values() {
        return attributes.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return attributes.entrySet();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(attributes);
    }

}
