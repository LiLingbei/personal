package org.lubei.bases.core.plugin.reader;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * XML文件读写工具类
 *
 * @author gaojuhua
 * @date 2015/09/07
 */
public class XmlReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlReader.class);

    private String path = ClassLoader.getSystemResource("").getPath();


    /**
     * 读XML文件
     *
     * @param clazz    : 读取文件的映射Java类
     * @param fileName : 文件名
     * @param <T>      : 泛型类型类
     * @return 对象实例
     * @throws IOException: IO异常
     */
    public <T> T readXmlFile(Class<T> clazz, String fileName) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        File file = new File(path + fileName);
        LOGGER.debug("ReadXmlFile : {}" , file.getAbsolutePath());
        //解析文件,生成实例
        T t = (T) xmlMapper.readValue(file, clazz);

        return t;

    }


    /**
     * 写对象实例到XML文件
     *
     * @param clazz    : 读取文件的映射Java类
     * @param fileName : 文件名
     * @param t        : 泛型类型实例
     * @throws IOException: IO异常
     */

    public <T> void writeXmlFile(Class<T> clazz, String fileName, T t) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        //对象实例写入文件
        xmlMapper.writeValue(new File(path + fileName), t);

    }

    /**
     * 文件是否存在
     *
     * @param fileName 文件名
     */
    public boolean isExist(String fileName) {
        File file = new File(path + fileName);
        LOGGER.debug("ReadXmlFile isExist : {}" , file.getAbsolutePath());
        return file.exists();
    }

    public String getPath() {
        return path;
    }

    /**
     * 设置路径
     *
     * @param path 路径
     */

    public void setPath(String path) {
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        this.path = path;
    }
}
