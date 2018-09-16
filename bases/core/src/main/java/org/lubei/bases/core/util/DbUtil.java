package org.lubei.bases.core.util;

import org.apache.commons.dbutils.RowProcessor;
import org.apache.ibatis.session.SqlSession;

import java.util.Iterator;
import java.util.List;

/**
 * 数据库操作工具类
 */
public class DbUtil {

    /**
     * 处理C_字段的行处理器，该处理器同时将支持将字段小写化放到map中。 用法举例：
     * <pre>
     * List<User> users = runner.query(sql, new BeanListHandler<User>(User.class,
     * DbUtil.COLUMN_LABEL_CONVERTER));
     * </pre>
     */
    public static RowProcessor COLUMN_LABEL_CONVERTER = new CRowProcessor();

    /**
     * 安静的关闭batis session，session可以为空
     *
     * @param session batis Session
     */
    public static void closeQuietly(SqlSession session) {
        if (session == null) {
            return;
        }
        session.close();
    }

    /**
     * 合并多个id到一个字符串供sql的in操作
     *
     * @param ids 要拼装的id列表
     * @return 以逗号分隔形式合并多个字符串，并在每个字符串两端补充单引号“'”
     */
    public static String joinSqlIds(List<String> ids) {
        Iterator<String> it = ids.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        if (it.hasNext()) {
            stringBuilder.append('\'').append(it.next()).append('\'');
            while (it.hasNext()) {
                stringBuilder.append(',');
                stringBuilder.append('\'');
                stringBuilder.append(it.next());
                stringBuilder.append('\'');
            }
        }
        return stringBuilder.toString();
    }

}
