package org.lubei.bases.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * 资源工具，用例从classpath读取文件
 */
public class ResourceUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);

    private ResourceUtil() {
        throw new IllegalAccessError("不能实例化");
    }

    /**
     * 从classpath根路径下读取文件内容
     *
     * @param resourceName 文件名称
     * @return 文件内容（UTF_8字符集）
     */
    public static String getString(final String resourceName) throws IOException {
        URL url = Resources.getResource(resourceName);
        return Resources.toString(url, Charsets.UTF_8);
    }

    /**
     * 从classpath中读取contextClass同路径下文件内容
     *
     * @param contextClass 文件所在路径下的任一class
     * @param resourceName 文件名称
     * @return 文件内容（UTF_8字符集）
     */
    public static String getString(final Class<?> contextClass, final String resourceName)
            throws IOException {
        URL url = Resources.getResource(contextClass, resourceName);
        return Resources.toString(url, Charsets.UTF_8);
    }

    /**
     * 从classpath根路径下读取文件内容到reader对象
     *
     * @param configFileName 文件名称
     * @return 文件内容的reader（UTF_8字符集），不需要关闭
     */
    public static StringReader getStringReader(final String configFileName) throws IOException {
        String s = getString(configFileName);
        return new StringReader(s);
    }

    /**
     * 从classpath中读取contextClass同路径下文件内容到reader对象
     *
     * @param contextClass   文件所在路径下的任一class
     * @param configFileName 文件名称
     * @return 文件内容的reader（UTF_8字符集），不需要关闭
     */
    public static StringReader getStringReader(final Class<?> contextClass,
                                               final String configFileName) throws IOException {
        String s = getString(contextClass, configFileName);
        return new StringReader(s);
    }

    /**
     * 从classpath根路径下读取文件并反序列化为对象
     *
     * @param configFileName 文件名称
     * @param clazz          所要反序列化的类型
     * @return 反序列化后的对象，（如果文件未找到等情况下，将产生运行时异常）
     */
    public static <T> T readJson(String configFileName, Class<T> clazz) {
        ClassLoader loader = MoreObjects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                Resources.class.getClassLoader());
        try {
            Enumeration<URL> resources = loader.getResources(configFileName);
            List<URL> urls = Lists.newArrayList();
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                urls.add(url);
            }
            JSONObject finalConf = new JSONObject();
            for (URL url : Lists.reverse(urls)) {
                String json = Resources.toString(url, Charsets.UTF_8);
                JSONObject tmpConf = JSON.parseObject(json);
                finalConf.putAll(tmpConf);
            }

            return TypeUtils.cast(finalConf, clazz, ParserConfig.getGlobalInstance());
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return null;
    }

    /**
     * 覆盖式写入配置文件
     *
     * @param fileName 配置文件名称或全部路径
     * @param prop     配置项集合
     * @throws IOException 写入失败时抛出IO异常
     */
    public static void overWriteToFile(String fileName, Properties prop) throws IOException {
        FileOutputStream oFile = new FileOutputStream(fileName, false);//true表示追加打开
        prop.store(oFile, "");
        oFile.close();
    }

    /**
     * 追加式写入配置文件
     *
     * @param fileName 配置文件名称或文件全路径
     * @param prop     配置项集合
     * @throws IOException 写入失败时抛出IO异常
     */
    public static void appendToFile(String fileName, Properties prop) throws IOException {
        FileOutputStream oFile = new FileOutputStream(fileName, true);//true表示追加打开
        prop.store(oFile, "");
        oFile.close();
    }

    /**
     * 从配置文件获取配置项集合
     *
     * @param fileName 配置文件名称或全路经
     * @return 配置型集合
     * @throws IOException 文件不存在返回IO异常
     */
    public static Properties getProperties(String fileName) throws IOException {

        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        Properties prop = new Properties();
        prop.load(in);     ///加载属性列表
        in.close();
        return prop;
    }

}
