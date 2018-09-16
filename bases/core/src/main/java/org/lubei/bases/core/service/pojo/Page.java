package org.lubei.bases.core.service.pojo;

import java.util.List;

/**
 * 用于分页显示数据，封装了数据结果集，总页数，总数据条数，每页显示数据大小，当前显示第几页数据 <br>
 * <p>
 * Create on : 2013-10-9<br>
 * </p>
 * <br>
 * 
 * @author FuMin<br>
 * @version its.dev.framework v1.0
 *          <p>
 *          <br>
 *          <strong>Modify History:</strong><br>
 *          user modify_date modify_content<br>
 *          -------------------------------------------<br>
 *          <br>
 *<table border="1px" width="600px">
 *<caption style="font-family:verdana;font-size:120%;font-weight:bolder;">分页对象属性说明</caption>
 * <thead>
 *  <tr bgcolor="#e1f7ff"><th>属性</th><th>数据类型</th><th>默认取值</th><th>备注</th></tr>
 * </thead>
 * <tbody align="left">
 * <tr><th>totalRecord</th><th>int</th><th>0</th><th>总数据条数</th></tr>
 * <tr><th>pageSize</th><th>int</th><th>15</th><th>每页显示数据条数</th></tr>
 * <tr><th>pageIndex</th><th>int</th><th>1</th><th>表示当前是第几页数据</th></tr>
 * <tr><th>totalPage</th><th>int</th><th>0</th><th>总数据页数</th></tr>
 * <tr><th>data</th><th>List<T></th><th>包含0个T的数组</th><th>当前页显示的数据</th></tr>
 * </tbody>
 * </table>
 */
@Deprecated
public final class Page<T>{
    private int totalRecord = 0;
    private int pageSize = 15;
    private int pageIndex = 1;
    private int totalPage = 0;
    private List<T> data = null;

    public Page() {
    }
    
    public Page(List<T> data) {
        this.data=data;
    }

    /**
     * Constructors.
     * 
     * @param totalRecord 数据总数
     * @param totalRecord 数据总页数
     * @param pageSize 每页显示数据条数
     * @param pageIndex 当前页数
     */
    public Page(int totalRecord, int totalPage, int pageSize, int pageIndex) {
        setTotalRecord(totalRecord);
        setPageSize(pageSize);
        setPageIndex(pageIndex);
        setTotalPage(totalPage);
    }

    /**
     * Constructors.
     * 
     * @param totalRecord 数据总数
     * @param totalRecord 数据总页数
     * @param pageSize 每页显示数据条数
     * @param pageIndex 当前页数
     * @param data 当前页中的数据
     */
    public Page(int totalRecord, int totalPage, int pageSize, int pageIndex, List<T> data) {
        setTotalRecord(totalRecord);
        setPageSize(pageSize);
        setPageIndex(pageIndex);
        setTotalPage(totalPage);
        this.data = data;
    }

    /**
     * @return totalPage - 总页数
     */
    public int getTotalPage() {
        return totalPage;
    }

    /**
     * @param totalPage - 总页数
     */
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * @return totalCount - 返回数据总条数
     */
    public int getTotalRecord() {
        return totalRecord;
    }

    /**
     * @param totalRecord - 设置数据总条数，如果totalCount小于等于0，就设置为0
     */
    public void setTotalRecord(int totalRecord) {
        if (totalRecord <= 0) {
            this.totalRecord = 0;
        } else {
            this.totalRecord = totalRecord;
        }
    }

    /**
     * @return pageSize - 返回每页显示数据条数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize - 设置每页显示数据条数，如果pageSize小于等于1，就设置为1
     */
    public void setPageSize(int pageSize) {
        if (pageSize <= 1) {
            this.pageSize = 1;
        } else {
            this.pageSize = pageSize;
        }
    }

    /**
     * @return currentPage - 当前显示页数
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * @param pageIndex - 当前显示页数.如果currentPage小于等于1，就设置为1
     */
    public void setPageIndex(int pageIndex) {
        if (pageIndex <= 1) {
            this.pageIndex = 1;
        } else {
            this.pageIndex = pageIndex;
        }
    }

    /**
     * @return dataList - 返回分页数据
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param data - 设置分页数据
     */
    public void setData(List<T> data) {
        this.data = data;
    }

}
