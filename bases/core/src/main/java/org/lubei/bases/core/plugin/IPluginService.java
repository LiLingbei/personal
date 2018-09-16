package org.lubei.bases.core.plugin;

import ro.fortsoft.pf4j.PluginState;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * 插件服务 插件应用入口，负责管理插件。 提供创建扩展实例、安装、卸载、启用、禁用服务。
 */
public interface IPluginService {

    String PF4J_DIR = "pf4j.pluginsDir";
    String DEFAULT_PF4J_DIR = "../plugins";

    /**
     * 获取插件指定的资源文件.
     *
     * @param pluginId     :插件Id.
     * @param resourceName :资源文件名称
     */
    URL getResource(final String pluginId, final String resourceName);

    /**
     * 初始化
     */
    void init();

    /**
     * 获取插件列表.
     *
     * @return List, 插件列表！
     */
    List<PluginWrapper> getPlugins();

    /**
     * 获取插件.
     *
     * @param pluginId 插件ID
     * @return plugin 插件
     */
    PluginWrapper getPlugin(String pluginId);

    /**
     * 启动插件.
     *
     * @param pluginId 插件ID
     * @return 插件状态
     */
    PluginState startPlugin(String pluginId);

    /**
     * 停止插件.
     *
     * @return 插件状态
     */
    PluginState stopPlugin(String pluginId);

    /**
     * 禁用插件.
     *
     * @param pluginId 插件ID
     * @return true 如果禁用成功，返回true
     */
    boolean disablePlugin(String pluginId);

    /**
     * 启用插件.
     *
     * @param pluginId 插件ID
     * @return true 如果启用成功，返回true
     */
    public boolean enablePlugin(String pluginId);

    /**
     * 通过插件ID、扩展点接口创建插件扩展实例
     *
     * @param pluginId    插件ID
     * @param parentClass 扩展点接口类
     * @return 扩展点实例
     */
    <T> T createExtension(String pluginId, Class<T> parentClass);

    /**
     * 通过插件ID、扩展点接口创建插件扩展实例
     *
     * @param pluginId       插件ID
     * @param extensionClass 扩展实现类
     * @return 扩展点实例
     */
    Object createExtension(final String pluginId, final String extensionClass);

    /**
     * 通过插件接口获取所有启用插件的接口实例
     *
     * @param extensionClass 扩展实现类
     * @return 扩展点实例的List
     */
    <T> List<T> getExtensions(final Class<T> extensionClass);

    /**
     * 通过插件ID获取所有扩展类名集合
     *
     * @param pluginId 插件ID
     * @return 扩展点类名Set
     */
    Set<String> getExtensionClassNames(String pluginId);

    /**
     * 安装插件.
     *
     * @param zip 安装包
     * @return true 如果安装成功，返回true
     */
    boolean installPlugin(File zip);

    /**
     * 卸载插件.
     *
     * @param pluginId 插件ID
     * @return true 如果卸载成功，返回
     */
    boolean uninstallPlugin(String pluginId);

    /**
     * 插件备份，暂不实现
     *
     * @param pluginId 插件ID
     * @return true, 如果备份成功！
     */
    boolean backup(String pluginId);

    /**
     * 插件回滚，暂不实现
     *
     * @param pluginId 插件ID
     * @return true, 如果回滚成功！
     */
    boolean rollback(String pluginId);

    /**
     * 插件监听器
     *
     * @param listener 监听器
     */
    void addListener(IPluginListener listener);
}
