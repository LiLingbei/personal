package org.lubei.bases.core.plugin;

/**
 * 插件监听器
 *
 * @author gaojuhua
 * @date 2015/09/14.
 */
public interface IPluginListener {

    /**
     * 插件安装时触发
     *
     * @param plugin 插件ID
     */
    boolean installing(String plugin);

    /**
     * 卸载插件时触发
     *
     * @param plugin 插件ID
     */
    boolean uninstalling(String plugin);

    /**
     * 插件加载式触发 .
     *
     * @param plugin ：插件名
     */
    boolean onLoading(String plugin);


}
