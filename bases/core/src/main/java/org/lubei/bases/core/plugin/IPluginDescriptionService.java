package org.lubei.bases.core.plugin;

import org.lubei.bases.core.plugin.pojo.Description;
import org.lubei.bases.core.plugin.pojo.PluginDescription;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 插件描述服务.
 *
 * @author gaojuhua.
 */
public interface IPluginDescriptionService {

    /**
     * 加载插件描述信息
     *
     * @param plugin 插件ID
     */
    void load(String plugin);

    /**
     * 卸载插件描述信息
     *
     * @param plugin 插件ID
     */
    void unload(String plugin);

    /**
     * 获取插件描述类型列表
     *
     * @return 描述类型列表
     */

    List<String> getDescriptionTypes(String plugin);

    /**
     * 获取插件描述列表
     *
     * @return 描述列表
     */

    List<JSONObject> getDescriptions(String type);

    /**
     * 获取插件描述列表
     *
     * @return 描述列表
     */

    List<PluginDescription> getPluginDescriptions();

    /**
     * 获取描述
     *
     * @param plugin 插件ID
     * @param type   描述类型
     * @return 描述列表
     */
    Description getDescription(String plugin, String type);

}
