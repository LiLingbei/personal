package org.lubei.bases.core.service;

import org.lubei.bases.core.db.Db;
import org.lubei.bases.core.service.cache.PojoCache;
import org.lubei.bases.core.service.pojo.BasePojo;
import org.lubei.bases.core.service.pojo.Page;
import org.lubei.bases.core.service.pojo.PageQueryParam;
import org.lubei.bases.core.service.pojo.QueryParam;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.List;

/**
 * 数据服务接口
 *
 * @param <T> bean类型
 * @param <K> ID类型<br>
 * @author panhongliang
 */
@SuppressWarnings("rawtypes")
@Deprecated
public interface IBaseService<T extends BasePojo, K> extends IService {

    /**
     * 服务id
     *
     * @return String
     */
    String getServiceId();

    /**
     * setDb.
     *
     * @param db Db
     */
    void setDb(Db db);

    /**
     * 按ID删除记录
     *
     * @param id 记录ID
     * @return 删除数目
     */
    int deleteById(K id);

    /**
     * 批量删除多条记录
     *
     * @param ids 记录ID列表
     * @return 删除数目
     */
    int deleteByIds(List<K> ids);

    /**
     * 根据参数批量删除对象
     *
     * @return String
     */
    int deleteByQuery(QueryParam<T> param);

    /**
     * 根据pojo属性批量删除对象
     *
     * @return String
     */
    int deleteByPojo(T pojo);

    /**
     * 获取所有记录
     *
     * @return 所有记录
     */
    List<T> getAll();

    /**
     * 按ID获取单行记录
     *
     * @param id 记录ID
     * @return 记录
     */
    T getById(K id);

    /**
     * 按ID获取单行记录
     *
     * @param id 记录ID
     * @return 记录
     */
    T getByIdFromCache(K id);

    /**
     * 按ID获取多条记录
     *
     * @param ids 记录ID类别
     * @return 记录列表
     */
    List<T> getByIds(List<K> ids);

    /**
     * 根据查询参数获取多条记录
     *
     * @param queryParam 对象pojo或扩展
     * @return 记录列表
     */
    List<T> getByQuery(QueryParam<T> queryParam);

    /**
     * 分页查询.
     *
     * @return List<T>
     */
    Page<T> getByPage(PageQueryParam<T> pageQueryParam);

    /**
     * 根据对象本身信息，进行过滤
     *
     * @return List<T>记录列表
     */
    List<T> getByPojo(T pojo);

    /**
     * 更新单条记录
     *
     * @param row 记录（ID中必须有值）
     * @return 修改数目
     */
    int modify(T row);

    /**
     * 选择性保存<br> 只保存非空字段
     *
     * @return 修改数目
     */
    int modifySelective(T row);

    /**
     * 批量选择性保存<br> 只保存非空字段
     *
     * @return 修改数目
     */
    int modifyBatchSelective(List<T> rows);

    /**
     * 保存一条记录
     *
     * @param row 记录
     * @return 修改数目
     */
    int save(T row);

    /**
     * 选择性保存<br> 只保存非空字段
     *
     * @param row 记录
     * @return 修改数目
     */
    int saveSelective(T row);

    /**
     * 批量保存对象
     *
     * @param rows 对象列表
     * @return 修改数目
     */
    int saveBatch(List<T> rows);

    /**
     * 批量保存对象 可以分批次保存 如 1000条数据，分10次保存，每次保存100条
     *
     * @param rows 对象列表
     * @param batchSize 每批保存的条数
     * @return 修改数目
     */
    public int saveBatch(final List<T> rows, int batchSize);

    /**
     * 执行sql脚本文件
     *
     * @param sqlScriptFile File
     * @return String 错误信息
     */
    String runSqlFile(final File sqlScriptFile);

    /**
     * 执行sql脚本文件
     *
     * @param sqlScript 文本内容
     * @return String 错误信息
     */
    String runSqlScript(final String sqlScript);

    /**
     * 获取缓存对象 getCache.
     *
     * @return String
     */
    PojoCache<T, K> getCache();

    /**
     * 按照json格式参数执行自定义查询获取对象列表
     * 该方法只适合小范围使用,使用前请确定Mapper文件中是否支持，另外支持种类只适合有限得几种，未全面测试
     * @param params ：内部对象定义字段，操作符，值对象，字段类型等{"fieldType":"String/Date/Number","dateQueryFlag":"0,=; ","fieldCode":"字段名","fieldValue":值对象}
     * @param orderByString ：列表排序字段
     * @return 对象列表
     */
    List<T> getByJSONParams(List<JSONObject> params, String orderByString);
}
