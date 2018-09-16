package org.lubei.bases.core.plugin.pf4j;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 插件配置文件读写工具类
 *
 * @author gaojuhua
 * @date 2015/07/04
 */
public class PluginFileUtil {
    public static final String ENABLED_FILE = "enabled.txt";
    public static final String DISABLED_FILE = "disabled.txt";
    public static final String INSTALLED_FILE = "installed.txt";
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginFileUtil.class);
    private static final String LINE_END = System.getProperty("line.separator");


    /**
     * 读文件，返回所有的行
     *
     * @param fileName 文件名
     * @return 文件中所有的行字符创
     */
    public static List<String> readFromFile(final String fileName) {
        checkNotNull(fileName, "文件名不能为空.");
        File file = new File(getPluginPath() + fileName);
        List<String> lines = Lists.newArrayList();
        //判断文件是否存在，不存在直接返回
        if (!file.exists()) {
            return lines;
        }
        try {
            lines = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("ERROR 读文件时发生错误 {}", e.toString());
        }
        return lines;
    }


    /**
     * 追加文件 追加前检查文件是否包含content内容
     *
     * @param fileName 文件名
     */
    public static void append(final String fileName, final String content) {
        checkNotNull(fileName, "文件名不能为空.");
        checkNotNull(content, "内容不能为空.");

        //检查是否存在内容
        boolean isExist = isExistContent(fileName, content);
        //文件中不存在content时
        if (isExist == false) {
            appendToFile(fileName, content);
        }

    }

    /**
     * 移除内容 移除前检查文件是否包含content内容
     *
     * @param fileName 文件名
     */
    public static void remove(final String fileName, final String content) {
        checkNotNull(fileName, "文件名不能为空.");
        checkNotNull(content, "内容不能为空.");
        //读文件
        List<String> contents = readFromFile(fileName);
        //
        List<String> newContents = Lists.newArrayList();
        //过滤要删除的内容
        for (String line : contents) {
            if (!line.equals(content)) {
                newContents.add(line);
            }
        }
        writeToFile(fileName, newContents);
    }

    /**
     * 判断文件中是否包含content内容
     *
     * @param fileName 文件名
     */
    private static boolean isExistContent(final String fileName, final String content) {

        //读文件
        List<String> lines = readFromFile(getPluginPath() + fileName);
        //判断文件中是否包含content内容
        if (lines != null && lines.size() > 0) {
            for (String line : lines) {
                if (content.equals(line)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 追加文件
     *
     * @param fileName 文件名
     * @param contents 文件内容
     */
    private static void appendToFile(final String fileName, final List<String> contents) {

        final File newFile = new File(fileName);
        try {
            for (String content : contents) {
                Files.append(content + LINE_END, newFile, Charsets.UTF_8);
            }

        } catch (IOException e) {
            LOGGER.error("ERROR 追加文件时发生错误 {}", e.toString());
        }
    }

    /**
     * 追加文件
     *
     * @param fileName 文件名
     * @param contents 文件内容
     */
    private static void appendToFile(final String fileName, final String contents) {

        final File newFile = new File(getPluginPath() + fileName);
        try {
            Files.append(contents + LINE_END, newFile, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("ERROR 追加文件时发生错误 {}", e.toString());
        }
    }

    /**
     * 写入文件
     *
     * @param fileName 文件名
     * @param contents 文件内容
     */
    private static void writeToFile(final String fileName, final List<String> contents) {
        final File newFile = new File(getPluginPath() + fileName);
        try {
            Files.write("", newFile, Charsets.UTF_8);
            for (String content : contents) {
                Files.append(content + LINE_END, newFile, Charsets.UTF_8);
            }
        } catch (IOException e) {
            LOGGER.error("ERROR 写文件时发生错误 {}", e.toString());
        }
    }

    /**
     * 写入文件
     *
     * @param fileName 文件名
     * @param content  文件内容
     */
    private static void writeToFile(final String fileName, final String content) {
        final File newFile = new File(getPluginPath() + fileName);
        try {

            Files.write(content + LINE_END, newFile, Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("ERROR 写文件时发生错误 {} ", e.toString());
        }
    }

    /**
     * 插件路径
     */
    private static String getPluginPath() {
        return System.getProperty(PluginService.PF4J_DIR) + File.separator;

    }
}
