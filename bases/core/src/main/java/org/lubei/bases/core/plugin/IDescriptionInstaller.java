package org.lubei.bases.core.plugin;

/**
 * 描述文件安装器
 *
 * @author gaojuhua.
 * @date 2015/9/17.
 */
public interface IDescriptionInstaller {

    /**
     * 安装描述文件
     *
     * @param plugin 插件名
     */
    void install(String plugin);

    /**
     * 卸载描述文件
     *
     * @param plugin 插件名
     */
    void uninstall(String plugin);

    /**
     * 插件加载后，执行操作.
     *
     * @param plugin :插件名
     */
    void load(String plugin);
}
