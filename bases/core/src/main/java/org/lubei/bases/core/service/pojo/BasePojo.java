package org.lubei.bases.core.service.pojo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <pre>
 * 数据库实体的抽象类，所有的数据库实体都需要继承该类，该类提供一些基本
 * 的数据库映射封装，比如主键，扩展字段等的映射。
 * </pre>
 */
@Deprecated
public abstract class BasePojo<K> implements Serializable {
    /**
     * <code>serialVersionUID</code> - 序列号id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ID. 列： table.id
     * 
     * @mbggenerated
     */
    protected K id;

    /**
     * json格式扩展信息. 列： table.json
     * 
     * @mbggenerated
     */
    private String json;

    /**
     * 冗余字段1. 列： table.tag1
     * 
     * @mbggenerated
     */
    private String tag1;

    /**
     * 冗余字段2. 列： table.tag2
     * 
     * @mbggenerated
     */
    private String tag2;

    /**
     * ID. 列： table.id
     * 
     * @return the value of table.id
     * 
     * @mbggenerated
     */
    public K getId() {
        return id;
    }

    /**
     * ID. 列： table.id
     * 
     * @param id the value for table.id
     * 
     * @mbggenerated
     */
    public void setId(final K id) {
        this.id = id;
    }


    /**
     * json格式扩展信息. 列： table.json
     * 
     * @return the value of table.json
     * 
     * @mbggenerated
     */
    public String getJson() {
        return json;
    }

    /**
     * json格式扩展信息. 列： table.json
     * 
     * @param json the value for table.json
     * 
     * @mbggenerated
     */
    public void setJson(final String json) {
        this.json = json == null ? null : json.trim();
    }

    /**
     * 冗余字段1. 列： table.tag1
     * 
     * @return the value of table.tag1
     * 
     * @mbggenerated
     */
    public String getTag1() {
        return tag1;
    }

    /**
     * 冗余字段1. 列： table.tag1
     * 
     * @param tag1 the value for table.tag1
     * 
     * @mbggenerated
     */
    public void setTag1(final String tag1) {
        this.tag1 = tag1 == null ? null : tag1.trim();
    }

    /**
     * 冗余字段2. 列： table.tag2
     * 
     * @return the value of table.tag2
     * 
     * @mbggenerated
     */
    public String getTag2() {
        return tag2;
    }

    /**
     * 冗余字段2. 列： table.tag2
     * 
     * @param tag2 the value for table.tag2
     * 
     * @mbggenerated
     */
    public void setTag2(final String tag2) {
        this.tag2 = tag2 == null ? null : tag2.trim();
    }

    @Override
    public String toString() {
        return com.alibaba.fastjson.JSON.toJSONString(this);
    }

    public Object deepClone() throws IOException, ClassNotFoundException {

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(this);
        oo.flush();
        oo.close();

        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }
}
