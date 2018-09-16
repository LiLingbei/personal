package org.lubei.bases.core.plugin.reader;

import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.plugin.IDescriptionReader;
import org.lubei.bases.core.plugin.pf4j.PluginService;
import org.lubei.bases.core.plugin.pojo.Description;
import org.lubei.bases.core.plugin.pojo.PluginDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Ci描述文件读取.
 *
 * @author gaojuhua.
 */
public class PluginDescriptionReader implements IDescriptionReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDescriptionReader.class);


    /**
     * XML文件解析器
     */
    private XmlReader xmlReader = new XmlReader();


    @Override
    public Description read(String plugin) {

        xmlReader.setPath(System.getProperty(PluginService.PF4J_DIR));
        //
        String fileName = plugin + CLASS_PATH + PluginDescription.PLUGIN + XML;
        //
        LOGGER.debug("读取插件描述文件 {}{} ", xmlReader.getPath(), fileName);
        PluginDescription description = null;
        HashMap<String, Object> config = null;
        try {
            //文件存在时
            if (xmlReader.isExist(fileName)) {
                //读取文件
                config = xmlReader.readXmlFile(HashMap.class, fileName);
                if (config != null) {
                    description = new PluginDescription(config);
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Read Plugin {}  ", plugin, e);
            throw new BusinessException(fileName + "文件解析异常！", e);
        }
        return description;

    }
}
