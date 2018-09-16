package org.lubei.bases.core.service.pojo;

import java.util.List;

/**
 * 数据库查询参数的封装, 在构造复杂查询（在构造分页，条件过滤，排序复杂查询的时候使用）<br>
 * <p>
 * * <table border="1px" width="600px">
 * <caption style="font-family:verdana;font-size:120%;font-weight:bolder;">属性使用说明</caption>
 * <thead>
 * <tr bgcolor="#e1f7ff"><th>属性</th><th>数据类型</th><th>默认取值</th><th>备注</th></tr>
 * </thead>
 * <tbody>
 * <tr><td colspan="4">从QueryParam继承的属性使用说明请参考{@link QueryParam}</td></tr>
 * <tr><td>pageSize</td><td>int</td><td>15</td><td>指示每页显示数据条数</td></tr>
 *  <tr><td>pageIndex</td><td>int</td><td>1</td><td>指示显示页数</td></tr>
 *  <tr><td>totalPage</td><td>int</td><td>0</td><td>指示总页数</td></tr>
 *  <tr><td>totalRecord</td><td>int</td><td>0</td><td>指示总数据数</td></tr>
 * </tbody>
 * </p> 
 * <p>
 * Create on : 2013-10-9<br>
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
 */
@Deprecated
public final class PageQueryParam<T> extends QueryParam<T>{
    private int pageSize = 15;
    private int pageIndex = 0;
    private int totalRecord = 0;
    private int totalPage = 0;
    /**
     * @return totalPage - 总页数，，默认值为0
     */
    public int getTotalPage() {
        return totalPage;
    }

    /**
     * @param totalPage - 总页数，，默认值为0
     */
    public void setTotalPage(int totalPage) {
        if (totalPage <= 0) {
            this.totalPage = 0;
        } else {
            this.totalPage = totalPage;
        }
        initParam();
    }

    /**
     * @return totalRecord - 总记录条数，默认值为0
     */
    public int getTotalRecord() {
        return totalRecord;
    }

    /**
     * @param totalRecord - 总记录条数，默认值为0
     */
    public void setTotalRecord(int totalRecord) {
        if (totalRecord <= 0) {
            this.totalRecord = 0;
        } else {
            this.totalRecord = totalRecord;
        }
        initParam();
    }

    /**
     * @return pageSize - 返回分页查询数据的时候用来控制每一页显示的数据条数
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize - 设置分页查询数据的时候用来控制每一页显示的数据条数
     */
    public void setPageSize(int pageSize) {
        if (pageSize <= 1) {
            this.pageSize = 1;
        } else {
            this.pageSize = pageSize;
        }
        initParam();
    }

    /**
     * @return currentPage - 页码，默认是第一页
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * @param pageIndex - 页码，默认是第一页
     */
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        initParam();
    }


    @Override
	public String toString() {
		return "PageQueryParam [pageSize=" + pageSize + ", pageIndex="
				+ pageIndex + ", totalRecord=" + totalRecord + ", totalPage="
				+ totalPage + "]";
	}

	/**
     * 初始化分页参数.依据总页数设置当前页数，计算总页数
     */
    private void initParam() {
        int totalPage = (this.totalRecord % this.pageSize) > 0 ? (this.totalRecord / this.pageSize + 1)
            : (this.totalRecord / this.pageSize);
        this.totalPage = totalPage;
        if (this.pageIndex > totalPage && totalPage > 0) {
            this.pageIndex = totalPage;
        }
    }

    /**
     * 依据当前分页查询参数设置分页接口分页信息.
     * 
     * @param <P>
     * @param page
     * @return
     */
    public <P> Page<P> initPage(final Page<P> page) {
        Page<P> initPage = page;
        if (initPage == null) {
            initPage = new Page<P>();
        }
        initPage.setPageIndex(pageIndex);
        initPage.setTotalPage(totalPage);
        initPage.setPageSize(pageSize);
        initPage.setTotalRecord(totalRecord);
        return initPage;
    }
    /**
     * 依据当前分页查询参数设置分页接口分页信息.
     * 
     * @param <P>
     * @param data List<P>
     * @return
     */
    public <P> Page<P> initPage(List<P> data) {
        Page<P> initPage = new Page<P>();
        initPage.setPageIndex(pageIndex);
        initPage.setTotalPage(totalPage);
        initPage.setPageSize(pageSize);
        initPage.setTotalRecord(totalRecord);
        initPage.setData(data);
        return initPage;
    }
}
