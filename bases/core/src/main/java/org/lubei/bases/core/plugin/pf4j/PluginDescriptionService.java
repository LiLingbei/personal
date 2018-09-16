package org.lubei.bases.core.plugin.pf4j;

import org.lubei.bases.core.plugin.IDescriptionReader;
import org.lubei.bases.core.plugin.IPluginDescriptionService;
import org.lubei.bases.core.plugin.pojo.Description;
import org.lubei.bases.core.plugin.pojo.PluginDescription;
import org.lubei.bases.core.service.IPortable;
import org.lubei.bases.core.util.ClassUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 插件描述服务实现
 *
 * @author gaojuhua.
 */
public class PluginDescriptionService implements IPluginDescriptionService, IPortable {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDescriptionService.class);

    /**
     * 插件描述Map
     */
    private Map<String, List<Description>> descriptionMap;
    /**
     * 描述文件读取列表
     */
    private List<IDescriptionReader> readers;

    /**
     * 构造方法
     */
    public PluginDescriptionService() {
        descriptionMap = Maps.newConcurrentMap();
        readers = Lists.newArrayList();
        init();
    }

    /**
     * 初始化
     */
    public void init() {
        try {
            // 插件描述读取实例化
            for (Class<?> clazz : ClassUtil.getAllAssignedClass(IDescriptionReader.class)) {
                Object reader = clazz.newInstance();
                readers.add((IDescriptionReader) reader);
            }
        } catch (Exception e) {
            LOGGER.warn("插件描述服务初始化错误! {}", e.getMessage(), e);
        }
    }


    @Override
    public void load(String plugin) {
        LOGGER.trace("加载插件描述信息：{}", plugin);
        //初始化
        List<Description> descriptions = Lists.newArrayList();
        //读取描述文件
        for (IDescriptionReader reader : readers) {
            try {
                Description description = reader.read(plugin);
                if (description != null) {
                    description.setPlugin(plugin);
                    descriptions.add(description);
                }
            } catch (Throwable e) {
                LOGGER.warn("解析插件描述异常！{}", plugin, e);
            }
        }
        descriptionMap.put(plugin, descriptions);

    }


    @Override
    public void unload(String plugin) {
        //初始化
        descriptionMap.put(plugin, Lists.newArrayList());
    }

    @Override
    public List<String> getDescriptionTypes(String plugin) {
        List<Description> descriptions = descriptionMap.get(plugin);
        return Lists.transform(descriptions, it -> it.getType());
    }

    @Override
    public List<JSONObject> getDescriptions(String type) {
        List<JSONObject> descriptions = Lists.newArrayList();
        for (Map.Entry<String, List<Description>> entry : descriptionMap.entrySet()) {
            for (Description description : entry.getValue()) {
                if (description.getType().equalsIgnoreCase(type)) {
                    descriptions.add(JSON.parseObject(JSON.toJSONString(description)));
                }
            }
        }
        return descriptions;
    }

    @Override
    public List<PluginDescription> getPluginDescriptions() {
        List<PluginDescription> descriptions = Lists.newArrayList();
        for (Map.Entry<String, List<Description>> entry : descriptionMap.entrySet()) {
            for (Description description : entry.getValue()) {
                if (description.getType().equalsIgnoreCase(PluginDescription.PLUGIN)) {
                    descriptions.add((PluginDescription) description);
                }
            }
        }
        return descriptions;
    }

    @Override
    public Description getDescription(String plugin, String type) {
        List<Description> descriptions = descriptionMap.get(plugin);
        Description description = null;
        // 匹配查找Type
        if (descriptions != null) {
            for (Description desc : descriptions) {
                if (desc.getType().equals(type)) {
                    description = desc;
                    break;
                }
            }
        }

        return description;
    }


}
