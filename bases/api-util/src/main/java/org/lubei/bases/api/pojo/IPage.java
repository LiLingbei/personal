package org.lubei.bases.api.pojo;

/**
 * 分页数据接口
 *
 * @author LiLingbei
 */
public interface IPage {

    /**
     * 获取每页条数
     *
     * @return 每页条数
     */
    Integer getPageSize();

    /**
     * 设置每页条数
     *
     * @param pageSize 每页条数
     */
    void setPageSize(Integer pageSize);

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    Integer getPageIndex();

    /**
     * 设置当前页码
     *
     * @param pageIndex 当前页码
     */
    void setPageIndex(Integer pageIndex);

    // 总页数、总条数返回前端时才需要

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    default Integer getTotalPage() {
        throw new AbstractMethodError();
    }

    /**
     * 设置总页数
     *
     * @param totalPage 总页数
     */
    default void setTotalPage(Integer totalPage) {
        throw new AbstractMethodError();
    }

    /**
     * 获取总条数
     *
     * @return 总条数
     */
    default Long getTotalRecord() {
        throw new AbstractMethodError();
    }

    /**
     * 设置总条数
     *
     * @param totalRecord 总条数
     */
    default void setTotalRecord(Long totalRecord) {
        throw new AbstractMethodError();
    }

}
