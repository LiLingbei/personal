package org.lubei.bases.api.pojo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页数据对象，封装了数据结果集，总页数，总条数，每页条数，当前页等信息
 *
 * @param <T> 数据类型
 * @author LiLingbei
 */
public final class Page<T> implements IPage, Serializable {

    private static final long serialVersionUID = 6068211310815072779L;

    private Long totalRecord = 0L;
    private Integer pageSize = 15;
    private Integer pageIndex = 1;
    private Integer totalPage = 0;
    private List<T> data = Collections.emptyList();

    /**
     * 构造函数
     */
    public Page() {
    }

    /**
     * 构造函数
     *
     * @param data 数据列表
     */
    public Page(List<T> data) {
        this.data = data;
    }

    /**
     * 构造函数
     *
     * @param totalRecord 总条数
     * @param totalPage   总页数
     * @param pageSize    每页条数
     * @param pageIndex   当前页
     */
    public Page(long totalRecord, int totalPage, int pageSize, int pageIndex) {
        setTotalRecord(totalRecord);
        setPageSize(pageSize);
        setPageIndex(pageIndex);
        setTotalPage(totalPage);
    }

    /**
     * 构造函数
     *
     * @param totalRecord 总条数
     * @param totalPage   总页数
     * @param pageSize    每页条数
     * @param pageIndex   当前页
     * @param data        数据列表
     */
    public Page(long totalRecord, int totalPage, int pageSize, int pageIndex, List<T> data) {
        setTotalRecord(totalRecord);
        setPageSize(pageSize);
        setPageIndex(pageIndex);
        setTotalPage(totalPage);
        this.data = data;
    }

    @Override
    public Integer getTotalPage() {
        return totalPage;
    }

    @Override
    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    @Override
    public Long getTotalRecord() {
        return totalRecord;
    }

    @Override
    public void setTotalRecord(Long totalRecord) {
        this.totalRecord = (totalRecord < 0 ? 0 : totalRecord);
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = (pageSize < 1 ? 1 : pageSize);
    }

    @Override
    public Integer getPageIndex() {
        return pageIndex;
    }

    @Override
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = (pageIndex < 1 ? 1 : pageIndex);
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 设置数据列表
     *
     * @param data 数据列表
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 创建一个新分页，不含数据
     *
     * @param <S> 新数据类型
     * @return 分页对象
     */
    public <S> Page<S> newPage() {
        Page<S> page = new Page<>();
        page.setTotalRecord(totalRecord);
        page.setPageSize(pageSize);
        page.setPageIndex(pageIndex);
        page.setTotalPage(totalPage);
        return page;
    }

}
