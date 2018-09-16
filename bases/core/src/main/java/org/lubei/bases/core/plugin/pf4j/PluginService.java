package org.lubei.bases.core.plugin.pf4j;

import static org.lubei.bases.core.service.ServicesFactory.getService;

import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.plugin.IPluginDescriptionService;
import org.lubei.bases.core.plugin.IPluginListener;
import org.lubei.bases.core.plugin.IPluginService;
import org.lubei.bases.core.service.IService;
import org.lubei.bases.core.util.ToJsonWrapper;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginState;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 插件服务实现.
 *
 * @author gaojuhua
 */
public class PluginService implements IPluginService, IService {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);
    /**
     * 监听器
     */
    private List<IPluginListener> listeners = Lists.newArrayList();
    /**
     * 插件管理器
     */
    private PluginManager pluginManager;

    @Override
    public URL getResource(String pluginId, String resourceName) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        return plugin.getPluginClassLoader().getResource(resourceName);
    }

    /**
     * 初始化
     */
    public void init() {
        LOGGER.info("插件服务初始化...");
        try {
            if (new File("../plugins").exists()) {
                System.setProperty(PF4J_DIR, "../plugins");
            }
            pluginManager = new DefaultPluginManager();
            loadPlugins();
            LOGGER.info("不再启动插件目录监视线程，避免重复加载");
//            new Thread(new PluginWatcher(this)).start();
            LOGGER.info("插件服务初始化完成!");
        } catch (Throwable e) {
            LOGGER.info("插件服务初始化失败!", e);
        }
    }

    /**
     * 获取插件列表.
     *
     * @return List, 插件列表！
     */
    @Override
    public List<PluginWrapper> getPlugins() {
        return this.pluginManager.getPlugins();

    }

    /**
     * 获取插件.
     *
     * @param pluginId 插件ID
     * @return plugin 插件
     */
    @Override
    public PluginWrapper getPlugin(final String pluginId) {
        return this.pluginManager.getPlugin(pluginId);
    }

    /**
     * 启动插件.
     *
     * @param pluginId 插件ID
     * @return 插件状态
     */
    @Override
    public PluginState startPlugin(final String pluginId) {
        PluginWrapper plugin = this.pluginManager.getPlugin(pluginId);
        //当插件没有被禁用时，执行启动方法
        if (PluginState.DISABLED != plugin.getPluginState()) {
            return this.pluginManager.startPlugin(pluginId);
        }
        return plugin.getPluginState();
    }

    @Override
    public PluginState stopPlugin(final String pluginId) {
        return this.pluginManager.stopPlugin(pluginId);
    }

    @Override
    public boolean disablePlugin(final String pluginId) {
        boolean result = this.pluginManager.disablePlugin(pluginId);
        //如果执行成功，在文件中记录插件ID
        if (result) {
            PluginFileUtil.append(PluginFileUtil.DISABLED_FILE, pluginId);
            PluginFileUtil.remove(PluginFileUtil.ENABLED_FILE, pluginId);
        }
        return result;
    }

    @Override
    public boolean enablePlugin(final String pluginId) {
        boolean result = this.pluginManager.enablePlugin(pluginId);
        //如果执行成功，在文件中记录插件ID
        if (result) {
            PluginFileUtil.append(PluginFileUtil.ENABLED_FILE, pluginId);
            PluginFileUtil.remove(PluginFileUtil.DISABLED_FILE, pluginId);
        }
        return result;
    }

    /**
     * 通过插件ID、扩展点接口创建插件扩展实例
     *
     * @param pluginId    插件ID
     * @param parentClass 扩展点接口类
     * @return 扩展点实例
     */
    @Override
    public <T> T createExtension(final String pluginId, final Class<T> parentClass) {

        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        Set<String> extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
        //没有扩展实现
        if (extensionClassNames == null) {
            LOGGER.error("extensionClassNames is null, {}  ", pluginId);
            throw new BusinessException(pluginId + "extensionClassNames is null!");
        }
        //遍历扩展类名，找到对应扩展接口的实现类
        for (String className : extensionClassNames) {
            try {
                Class clazz = plugin.getPluginClassLoader().loadClass(className);
                //判断clazz是否为parentClass的子类或实现类
                if (parentClass.isAssignableFrom(clazz)) {
                    T extension = (T) clazz.newInstance();
                    return extension;
                }
            } catch (Exception e) {
                LOGGER.error("createExtension {} {} ", className, e.getMessage(), e);
                throw new BusinessException(e);
            }


        }
        return null;
    }

    /**
     * 通过插件ID、扩展点接口创建插件扩展实例
     *
     * @param pluginId       插件ID
     * @param extensionClass 扩展实现类
     * @return 扩展点实例
     */
    @Override
    public Object createExtension(final String pluginId, final String extensionClass) {

        PluginWrapper plugin = pluginManager.getPlugin(pluginId);

        if (plugin == null || plugin.getPluginState().equals(PluginState.DISABLED)) {
            String msg = "Plugin:" + pluginId + " is null or is disabled";
            LOGGER.error(msg);
            throw new BusinessException(msg);
        }
        try {
            Class clazz = plugin.getPluginClassLoader().loadClass(extensionClass);
            Object extension = clazz.newInstance();
            return extension;
        } catch (Exception e) {
            LOGGER.error("createExtension {} {}", extensionClass, e.getMessage(), e);
            throw new BusinessException(e);
        }

    }

    @Override
    public <T> List<T> getExtensions(Class<T> extensionClass) {
        List<T> extensions = pluginManager.getExtensions(extensionClass);
        return extensions.stream().filter(it -> Objects.nonNull(it)).collect(Collectors.toList());
    }

    @Override
    public Set<String> getExtensionClassNames(String pluginId) {
        return pluginManager.getExtensionClassNames(pluginId);
    }

    /**
     * 安装插件.
     *
     * @param zip 安装包
     * @return true 如果安装成功，返回true
     */
    @Override
    public boolean installPlugin(final File zip) {
        String pluginId = this.pluginManager.loadPlugin(zip.toPath());
        //如果安装成功，执行插件初始化，并记录插件ID
        if (pluginId != null) {
            //to do 插件初始化
            PluginFileUtil.append(PluginFileUtil.INSTALLED_FILE, pluginId);
            for (IPluginListener listener : listeners) {
                listener.installing(pluginId);
            }
            return true;
        }
        return false;
    }

    /**
     * 卸载插件.
     *
     * @param pluginId 插件ID
     * @return true 如果卸载成功，返回
     */
    @Override
    public boolean uninstallPlugin(final String pluginId) {
        //删除插件文件
        boolean deleted = pluginManager.deletePlugin(pluginId);
        if (deleted) {
            PluginFileUtil.remove(PluginFileUtil.INSTALLED_FILE, pluginId);
            for (IPluginListener listener : listeners) {
                listener.installing(pluginId);
            }
        }

        return deleted;
    }

    /**
     * 插件备份，暂不实现
     *
     * @param pluginId 插件ID
     * @return true, 如果备份成功！
     */
    @Override
    public boolean backup(final String pluginId) {
        //TODO
        return false;
    }

    /**
     * 插件回滚，暂不实现
     *
     * @param pluginId 插件ID
     * @return true, 如果回滚成功！
     */
    @Override
    public boolean rollback(final String pluginId) {
        //TODO
        return false;
    }

    @Override
    public void addListener(IPluginListener listener) {
        this.listeners.add(listener);
    }


    /**
     * 加载插件
     */

    public void loadPlugins() {
        pluginManager.loadPlugins();
        List<PluginWrapper> plugins = this.getPlugins();
        IPluginDescriptionService service = getService(IPluginDescriptionService.class);
        for (PluginWrapper plugin : plugins) {
            if (plugin.getPluginState().equals(PluginState.DISABLED)) {
                continue;
            }
            String pluginId = plugin.getPluginId();
            LOGGER.trace("begin load plugin:{}", pluginId);
            service.load(pluginId);
            for (IPluginListener listener : listeners) {
                listener.onLoading(pluginId);
            }
            try {
                pluginManager.startPlugin(pluginId);
            } catch (Throwable e) {
                LOGGER.warn("plugin:{} start failed,may not implements Plugin Class", pluginId, e);
            }
            LOGGER.trace("end load plugin:{}", pluginId);
        }

    }

    /**
     * 同步插件
     */
    public void syncPlugins() {
        //获取已加载的插件
        List<PluginWrapper> plugins = this.getPlugins();
        List<String> installedPlugins =
                PluginFileUtil.readFromFile(PluginFileUtil.INSTALLED_FILE);

        for (PluginWrapper plugin : plugins) {
            //判断是否首次安装
            if (!installedPlugins.contains(plugin.getPluginId())) {
                installing(plugin);
            }
            //
            installedPlugins.remove(plugin.getPluginId());
        }
        // 插件文件夹已删除，需要卸载插件
        for (String pluginId : installedPlugins) {
            uninstalling(pluginId);

            //配置文件中移除
            PluginFileUtil.remove(PluginFileUtil.INSTALLED_FILE, pluginId);
        }
    }

    private void uninstalling(String pluginId) {
        //卸载插件
        for (IPluginListener listener : listeners) {
            listener.uninstalling(pluginId);
        }
    }

    /**
     * 首次安装插件触发
     */
    private void installing(PluginWrapper plugin) {
        //初始化插件
        try {
            boolean result = true;
            for (IPluginListener listener : listeners) {
                if (!listener.installing(plugin.getPluginId())) {
                    result = false;
                }
            }
            //记录到配置文件
            if (result) {
                PluginFileUtil.append(PluginFileUtil.INSTALLED_FILE, plugin.getPluginId());
            }
        } catch (Exception e) {
            LOGGER.warn("安装插件失败! {}", ToJsonWrapper.wrap(plugin), e);
        }
    }
}
