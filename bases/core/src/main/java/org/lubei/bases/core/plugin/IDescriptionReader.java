package org.lubei.bases.core.plugin;

import org.lubei.bases.core.plugin.pojo.Description;

import java.io.File;

/**
 * 描述文件读取器
 *
 * @author gaojuhua.
 * @date 2015/9/17.
 */
public interface IDescriptionReader {
    String XML = ".xml";
    String CLASS_PATH = File.separator + "classes" + File.separator;

    /**
     * 读取描述文件
     *
     * @param plugin 插件名
     */
    Description read(String plugin);
}
