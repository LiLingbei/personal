package org.lubei.bases.common.util;

import static org.lubei.bases.common.App.db;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.lubei.bases.api.pojo.IPage;
import org.lubei.bases.api.pojo.Page;

import java.util.List;
import java.util.function.Supplier;

/**
 * Mapper工具类
 *
 * @author LiLingbei
 */
public class Mappers {

    /**
     * 获取Mapper方法
     *
     * @param clazz 要获取Mapper的Class
     * @param <T>   Mapper泛型
     * @return Mapper对象
     */
    public static <T> T getMapper(Class<T> clazz) {
        return db.getMapper(clazz);
    }

    /**
     * 获取分页对象
     *
     * @param info PageInfo对象
     * @param <T>  数据泛型
     * @return 分页对象
     */
    public static <T> Page<T> getPage(PageInfo<T> info) {
        return setDataForPage(onlyPage(info), info.getList());
    }

    /**
     * 获取分页对象
     *
     * @param info PageInfo对象
     * @param data 数据对象List
     * @param <T>  数据泛型
     * @return 分页对象
     */
    public static <T> Page<T> getPage(PageInfo info, List<T> data) {
        return setDataForPage(onlyPage(info), data);
    }

    /**
     * 获取不含数据只有分页信息的分页对象
     *
     * @param info PageInfo对象
     * @param <T>  数据泛型
     * @return 分页对象
     */
    public static <T> Page<T> onlyPage(PageInfo info) {
        Page<T> page = new Page<>();
        page.setTotalRecord(info.getTotal());
        page.setPageSize(info.getPageSize());
        page.setPageIndex(info.getPageNum());
        page.setTotalPage(info.getPages());
        return page;
    }

    /**
     * 设置分页数据
     *
     * @param page 分页对象
     * @param data 数据List
     * @param <T>  数据泛型
     * @return 分页对象
     */
    public static <T> Page<T> setDataForPage(Page<T> page, List<T> data) {
        page.setData(data);
        return page;
    }

    /**
     * 获取空的分页对象
     *
     * @param pageSize 每页条数
     * @param <T>      数据泛型
     * @return 空的分页对象
     */
    public static <T> Page<T> emptyPage(int pageSize) {
        return emptyPage(pageSize, 1);
    }

    /**
     * 获取空的分页对象
     *
     * @param pageSize  每页条数
     * @param pageIndex 当前页码
     * @param <T>       数据泛型
     * @return 空的分页对象
     */
    public static <T> Page<T> emptyPage(int pageSize, int pageIndex) {
        Page<T> page = new Page<>();
        page.setPageSize(pageSize);
        page.setPageIndex(pageIndex);
        return page;
    }

    /**
     * 带分页数据库查询操作
     *
     * @param page     分页参数
     * @param supplier 查询操作
     * @param <R>      返回类型
     * @return 分页数据
     */
    public static <R> PageInfo<R> page(IPage page, Supplier<List<R>> supplier) {
        return page(page.getPageIndex(), page.getPageSize(), supplier);
    }

    /**
     * 带分页数据库查询操作
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param supplier 查询操作
     * @param <R>      返回类型
     * @return 分页数据
     */
    public static <R> PageInfo<R> page(int pageNum, int pageSize, Supplier<List<R>> supplier) {
        PageHelper.startPage(pageNum, pageSize);
        try {
            List<R> list = supplier.get();
            return new PageInfo<>(list);
        } finally {
            PageHelper.clearPage();
        }
    }

}
