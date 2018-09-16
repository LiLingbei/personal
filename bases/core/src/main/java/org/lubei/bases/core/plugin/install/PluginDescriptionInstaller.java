package org.lubei.bases.core.plugin.install;

import static org.lubei.bases.core.service.ServicesFactory.getService;

import org.lubei.bases.core.plugin.IDescriptionInstaller;
import org.lubei.bases.core.plugin.IPluginDescriptionService;
import org.lubei.bases.core.plugin.IPluginService;
import org.lubei.bases.core.plugin.pojo.PluginDescription;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 插件描述文件安装.
 *
 * @author gaojuhua.
 */
public class PluginDescriptionInstaller implements IDescriptionInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDescriptionInstaller.class);
    private static final String WEBAPP = "webapp";
    private static final String WEB_INF = "WEB-INF";
    private static final String WEB = "/../web";

    @Override
    public void install(String plugin) {
        //获取插件描述
        PluginDescription description = getPluginDescription(plugin);
        if (description == null) {
            description = new PluginDescription();
            description.setName("未知");
        }
        //插件根路径
        String pluginRoot = System.getProperty(
                IPluginService.PF4J_DIR, IPluginService.DEFAULT_PF4J_DIR);
        //插件Web路径
        String pluginPath = Joiner.on(File.separator)
                .join(Lists.newArrayList(pluginRoot, description.getName(), WEBAPP));
        //插件Web
        File pluginWeb = new File(pluginPath);
        if (pluginWeb.exists()) {
            File webRoot = new File(pluginRoot + WEB);
            if (webRoot.exists()) {
                File webapp = getWebApp(webRoot);
                if (webapp != null) {
                    try {
                        copy(pluginWeb, webapp.getParentFile());
                    } catch (IOException e) {
                        LOGGER.warn("Plugin move webapp error！", e);
                    }
                }
            }
        }

    }


    @Override
    public void uninstall(String plugin) {

    }

    @Override
    public void load(String plugin) {

    }

    /**
     * 获取插件描述
     *
     * @param plugin 插件名
     * @return 插件描述
     */

    private PluginDescription getPluginDescription(String plugin) {
        IPluginDescriptionService service = getService(IPluginDescriptionService.class);
        //
        return (PluginDescription) service.getDescription(plugin, PluginDescription.PLUGIN);
    }

    /**
     * 获取WebApp目录
     *
     * @param webRoot webRoot
     * @return File
     */
    private File getWebApp(File webRoot) {
        if (webRoot.isFile()) {
            return null;
        } else if (webRoot.getName().endsWith(WEB_INF)) {
            return webRoot;
        } else {
            File[] files = webRoot.listFiles();
            for (File file : files) {
                File webapp = getWebApp(file);
                if (webapp != null) {
                    return webapp;
                }
            }
        }
        return null;
    }

    /**
     * 拷贝文件
     *
     * @param source 源文件
     * @param target 目的文件
     * @throws IOException IOException
     */
    private void copy(File source, File target) throws IOException {

        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }
            File[] list = source.listFiles();
            for (int i = 0; i < list.length; i++) {
                File newSource = new File(source.getPath() + File.separator + list[i].getName());
                File newTarget = new File(target.getPath() + File.separator + list[i].getName());
                copy(newSource, newTarget);
            }
        } else if (source.isFile()) {
            if (target.exists()) {
                target.delete();
            }
            Files.copy(source.toPath(), target.toPath());
        } else {
            LOGGER.warn("请输入正确的文件名或路径名");
        }
    }
}
