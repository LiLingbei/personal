package org.lubei.bases.common.db.patcher;

import org.lubei.bases.common.App;
import org.lubei.bases.core.db.DbRunner;
import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.util.ResourceUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.h2.util.ScriptReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Created by sany on 16-2-29.
 */
public class Runner {

    public static final String JOB_DB_PART_MAN_SQL = "com/its/itone/job/db_part_man.sql";
    static final String LAST_DB_PATCH =
            "CREATE OR REPLACE FUNCTION public.last_db_patcher(OUT lastscript character varying)\n"
            + " RETURNS character varying\n"
            + " LANGUAGE plpgsql\n"
            + "AS $function$\n"
            + "BEGIN\n"
            + "    lastScript='%s';\n"
            + "END\n"
            + "$function$";
    static final String DB_PATCH_PATH = "dbPatchPath";
    static final String GET_LAST_SCRIPT = "SELECT last_db_patcher()";
    static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);
    static final String NULL_STRING = "null";
    private final DbRunner dbRunner;

    public Runner() {
        dbRunner = App.db.getRunner();
    }

    /**
     * 返回基线版本，从最低版本执行
     */
    public void base() {
        LOGGER.debug("base");
        String path = (String) App.getAppConfig().getParams().get(DB_PATCH_PATH);
        String[] scripts = readScriptList(path, null);
        execute(scripts, path, "");
    }

    /**
     * 从最后一次执行的最高版本号进行版本合并
     */
    public void merge() {
        partMan();
        LOGGER.debug("merge");
        String path = (String) App.getAppConfig().getParams().get(DB_PATCH_PATH);
        LOGGER.debug("patch Path:{}", path);
        String last = getLastScript();
        LOGGER.debug("last script version:{}", last);
        String[] scripts = readScriptList(path, last);
        LOGGER.debug("scripts:{}", new Object[]{scripts});
        execute(scripts, path, last);
    }

    private String getLastScript() {
        try {
            return dbRunner.query(GET_LAST_SCRIPT, new ScalarHandler<>());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从目录中读出有序的列表
     */
    private String[] readScriptList(String path, String lastScriptName) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory() == false) {
            return new String[]{};
        }
        String[] newScripts = file.list((dir, name) -> compare(lastScriptName, name));
        // 按照默认命名规则，可以直接使用默认排序
        Arrays.sort(newScripts);
        return newScripts;
    }

    /**
     * 判断文件版本和之前最后一个执行的版本的大小，比上次版本大则返回true，否则false
     *
     * @param lastScriptName 上次运行最大版本
     * @param name           当前文件
     */
    private boolean compare(String lastScriptName, String name) {
        if (Strings.isNullOrEmpty(lastScriptName)) {
            return true;
        }
        //因之前程序错误造成的null版本，默认从第一个文件重新开始升级
        if (lastScriptName.equalsIgnoreCase(NULL_STRING)) {
            return true;
        }
        //TODO 校验，当脚本不满足命名规则，直接退出
        return lastScriptName.compareTo(name) < 0;
    }

    /**
     * 分区维护
     */
    public void partMan() {
        try (Connection connection = App.db.getDataSource().getConnection()) {
            ScriptRunner runner = new ScriptRunner(connection);
            StringReader stringReader = ResourceUtil.getStringReader(JOB_DB_PART_MAN_SQL);
            runner.runScript(stringReader);
        } catch (Exception e) {
            LOGGER.warn("part_man fail", e);
        }
    }

    /**
     * 执行脚本
     */
    private void execute(String[] scripts, String path, String lastScript) {
        if (scripts.length <= 0) {
            return;
        }

        try (Connection connection = App.db.getDataSource().getConnection();
             Statement stat = connection.createStatement()) {
            File dir = new File(path);
            for (String script : scripts) {
                LOGGER.debug("execute dbpatcher:{}", script);
                File file = new File(dir, script);
                try (BufferedReader reader = Files.newReader(file, Charsets.UTF_8)) {
                    //原来使用mybatis的执行器冗余输出太多，而且吞掉了异常，替换成hsql的解析器执行
                    executeSqlFile(reader, stat, connection);
                    lastScript = script;
                } catch (Exception e) {
                    LOGGER.error("db patcher execute failed!\n"
                                 + "last success script:{}\n"
                                 + "error script:{}\n", lastScript, script, e);
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new BusinessException("db脚本执行失败！", e);
        } finally {
            if (lastScript != null) { //记录最后版本
                saveVersion(lastScript);
            }
        }
    }

    private void executeSqlFile(BufferedReader reader, Statement stat,
                                Connection connection)
            throws SQLException {

        ScriptReader r = new ScriptReader(reader);
        String sql = null;
        try {
            connection.setAutoCommit(false);
            while (true) {
                sql = r.readStatement();
                if (sql == null) {
                    break;
                }
                if (sql.trim().length() == 0) {
                    continue;
                }
                stat.execute(sql);
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            LOGGER.error("execute sql:{}", sql);
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void saveVersion(String lastScript) {
        String sql = String.format(LAST_DB_PATCH, lastScript);
        try {
            dbRunner.update(sql);
        } catch (Exception e) {
            LOGGER.error("记录数据库补丁脚本名称失败!", e);
        }
    }

}
