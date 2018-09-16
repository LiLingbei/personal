package org.lubei.bases.core.service.pojo;

import org.lubei.bases.core.util.CollectionUtils;
import org.lubei.bases.core.util.NamingUtils;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 数据库查询参数 <br>
 * <p>
 * 构造排序，条件过滤查询的时候使用。
 * <table border="1px" width="600px">
 * <caption style="font-family:verdana;font-size:120%;font-weight:bolder;">属性使用说明</caption> <thead>
 * <tr bgcolor="#e1f7ff">
 * <th>属性</th>
 * <th>数据类型</th>
 * <th>默认取值</th>
 * <th>备注</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>sortColumns</td>
 * <td>String[]</td>
 * <td>无</td>
 * <td>指示数据排序列</td>
 * </tr>
 * <tr>
 * <td>sortTypes</td>
 * <td>String[]</td>
 * <td>无</td>
 * <td>指示数据排序列的排序类型，参考值为ASC和DESC，默认为ASC，与sortColumns结合使用</td>
 * </tr>
 * <tr>
 * <td>pojo</td>
 * <td>泛型T</td>
 * <td>无</td>
 * <td>
 * 用户构造基本查询的参数对象（基本查询是指针对单个数据字段的过滤条件只有一个的查询情况，比如查询年龄大于等于25的用户就属于基本查询，查询年龄大于等于25并且年龄小于等于60的查询属于复杂查询）</td>
 * </tr>
 * <tr>
 * <td>param</td>
 * <td>Map<String, Object></td>
 * <td>size为零的map对象</td>
 * <td>用户构造复杂查询的参数对象（复杂查询是指至少存在一个针对单个数据字段的过滤条件大于一个的查询情况，比如查询年龄大于等于25并且年龄小于等于60的查询属于复杂查询）</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * Create on : 2013-10-17<br>
 * <p>
 * </p>
 * <br>
 *
 * @author FuMin<br>
 * @version its.dev.framework v1.0
 *          <p/>
 *          <br>
 *          <strong>Modify History:</strong><br>
 *          user modify_date modify_content<br>
 *          -------------------------------------------<br>
 *          <br>
 */
@Deprecated
public class QueryParam<T> {

    /**
     * 数据库升序排序方式
     */
    public static final String SORT_ASC = "ASC";

    /**
     * 数据库将序排序方式
     */
    public static final String SORT_DESC = "DESC";


    private String[] sortColumns;
    private String[] sortTypes;

    /**
     * 字段是否 区分大小写 //列名使用双引号,首字母转为大写
     */
    private boolean isColumnDoubleQuote = false;
    private boolean[] sortIsNumbers;
    private String customOrderByClause;
    private String dbType = DbType.Postgresql.toString();
    private T pojo;
    /**
     * 动态扩展参数<br>
     * 如： <if test="param['_c_region_ids'] != null "><br>
     * and ip.c_region_id IN <br>
     * <foreach collection="param['_c_region_ids']" item="item" index="index" open="(" separator=","
     * close=")"><br>
     * '${item}'<br>
     * </foreach><br>
     * </if>
     */
    private Map<String, Object> param = new HashMap<String, Object>();

    /**
     * @return pojo - 查询参数pojo对象
     */
    public T getPojo() {
        return pojo;
    }

    /**
     * @param pojo - 查询参数pojo对象
     */
    public void setPojo(final T pojo) {
        this.pojo = pojo;
    }

    /**
     * @return sortColumn - 排序列
     */
    public String[] getSortColumns() {
        return sortColumns;
    }

    /**
     * @param sortColumns - 排序列.
     */
    public void setSortColumns(final String[] sortColumns) {
        this.sortColumns = sortColumns;
    }

    /**
     * @return sortType - 排序类型
     */
    public String[] getSortTypes() {
        return sortTypes;
    }

    /**
     * @param sortTypes - 排序类型.
     */
    public void setSortTypes(final String[] sortTypes) {
        this.sortTypes = sortTypes;
    }


    /**
     * @return param - 扩展查询参数
     */
    public Map<String, Object> getParam() {
        return param;
    }

    /**
     * @param param - 扩展查询参数
     */
    public void setParam(final Map<String, Object> param) {
        this.param = param;
    }

    /**
     * 设置单列排序条件
     *
     * @param sortColumn
     * @param sortType
     * @param isNumber
     */
    public void setSortClause(final String sortColumn, final String sortType, final boolean isNumber) {
        this.sortColumns = new String[]{sortColumn};
        this.sortTypes = new String[]{sortType};
        this.sortIsNumbers = new boolean[]{isNumber};
    }

    /**
     * 特殊需求，可以自定义排序字符串，customOrderByClause
     * 如字段名大小写混合，表名有前缀等
     *
     * @return 排序参数
     */
    public String getOrderByClause() {
        if(!Strings.isNullOrEmpty(customOrderByClause)){
            return customOrderByClause;
        }
        if (null == sortColumns || sortColumns.length <= 0) {
            return null;
        }
        richSortColumns();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sortColumns.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            if (isSortByNumber(i)) {
                if (dbType.equalsIgnoreCase(DbType.Postgresql.toString())) {
                    builder.append("CAST(" + sortColumns[i] + " AS BIGINT)");
                } else if (dbType.equalsIgnoreCase(DbType.MySql.toString())) {
                    builder.append("(" + sortColumns[i] + "+0)");
                } else if (dbType.equalsIgnoreCase(DbType.MSSqlServer.toString())) {
                    builder.append("CAST(" + sortColumns[i] + " AS BIGINT)");
                } else if (dbType.equalsIgnoreCase(DbType.Oracle.toString())) {
                    builder.append("TO_NUMBER (" + sortColumns[i] + ")");
                }
            } else {
                builder.append(sortColumns[i]);
            }
            if (null == sortTypes || Strings.isNullOrEmpty(sortTypes[i])) {
                builder.append(" ").append(SORT_ASC);
            } else {
                builder.append(" ").append(sortTypes[i]);
            }

        }

        return builder.toString();
    }

    private void richSortColumns() {
        if (isColumnDoubleQuote()) {
            for (int i = 0; i < sortColumns.length; i++) {
                if (!sortColumns[i].startsWith("\"")) {
                    sortColumns[i] = "\"" + NamingUtils.firstUppercase(sortColumns[i]) + "\"";
                }
            }
            return;
        }
        for (int i = 0; i < sortColumns.length; i++) {
            if (!sortColumns[i].startsWith("c_")) {
                sortColumns[i] = "c_" + NamingUtils.camelCaseStringToColumnName(sortColumns[i]);
            }
        }
    }

    /**
     * isSortByNumber.
     *
     * @param i start form zero 0
     * @return boolean
     */
    private boolean isSortByNumber(final int i) {
        return null != sortIsNumbers && sortIsNumbers.length > i && sortIsNumbers[i];
    }

    /**
     * @return sortIsNumbers -sortIsNumbers{return content description}
     */
    public final boolean[] getSortIsNumbers() {
        return sortIsNumbers;
    }

    /**
     * @param sortIsNumbers - sortIsNumbers{parameter description}.
     */
    public final void setSortIsNumbers(final boolean[] sortIsNumbers) {
        this.sortIsNumbers = sortIsNumbers;
    }

    /**
     * @return dbType -dbType{return content description}
     */
    public final String getDbType() {
        return dbType;
    }

    /**
     * @param dbType - dbType{parameter description}.
     */
    public final void setDbType(final String dbType) {
        this.dbType = dbType;
    }

    public static List<Integer> stringsToIntegers(final List<String> datas) {
        return CollectionUtils.stringsToIntegers(datas);
    }

    public static List<Long> stringsToLongs(final List<String> datas) {
        return CollectionUtils.stringsToLongs(datas);
    }

    public boolean isColumnDoubleQuote() {
        return isColumnDoubleQuote;
    }

    public void setColumnDoubleQuote(boolean isColumnDoubleQuote) {
        this.isColumnDoubleQuote = isColumnDoubleQuote;
    }

    public String getCustomOrderByClause() {
        return customOrderByClause;
    }

    public void setCustomOrderByClause(String customOrderByClause) {
        this.customOrderByClause = customOrderByClause;
    }
}
