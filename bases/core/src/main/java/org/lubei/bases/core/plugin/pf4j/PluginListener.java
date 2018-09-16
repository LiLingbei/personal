package org.lubei.bases.core.plugin.pf4j;

import static org.lubei.bases.core.service.ServicesFactory.getService;

import org.lubei.bases.core.exception.BusinessException;
import org.lubei.bases.core.plugin.IDescriptionInstaller;
import org.lubei.bases.core.plugin.IPluginDescriptionService;
import org.lubei.bases.core.plugin.IPluginListener;
import org.lubei.bases.core.util.ClassUtil;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 插件监听器
 *
 * @author gaojuhua
 * @date 2015/09/14.
 */
public class PluginListener implements IPluginListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginListener.class);
    private static final String PLUGIN_CONFIG_JSON = "its_plugin_config.json";
    /**
     * 描述安装器列表
     */
    private List<IDescriptionInstaller> installers;

    /**
     * 构造方法
     */
    public PluginListener() {
        installers = Lists.newArrayList();
        init();
    }

    @Override
    public boolean installing(String plugin) {
        try {
            //读取描述文件
            for (IDescriptionInstaller installer : installers) {
                installer.install(plugin);
            }
            //安装器为空时
            if (installers.size() == 0) {
                return false;
            }
        } catch (Exception e) {
            LOGGER.warn("installing {} ", plugin, e);
            throw new BusinessException("安装插件异常-" + plugin, e);
        }
        return true;
    }

    @Override
    public boolean uninstalling(String plugin) {
        try {
            IPluginDescriptionService service = getService(IPluginDescriptionService.class);

            //读取描述文件
            for (IDescriptionInstaller installer : installers) {
                installer.uninstall(plugin);
            }
            //安装器为空时
            if (installers.size() == 0) {
                return false;
            }
            service.unload(plugin);

        } catch (Exception e) {
            LOGGER.warn("installing {}", plugin, e);
            throw new BusinessException("卸载插件异常-" + plugin, e);

        }
        return true;
    }

    @Override
    public boolean onLoading(String plugin) {
        try {
            //读取描述文件
            for (IDescriptionInstaller installer : installers) {
                LOGGER.debug("onLoading: installer {}, plugin {}", installer, plugin);
                installer.load(plugin);
            }
        } catch (Exception e) {
            LOGGER.warn("installing {}", plugin, e);
            throw new BusinessException("加载插件异常-" + plugin, e);
        }
        return true;
    }

    /**
     * 初始化
     */
    public void init() {
        try {
            for (Class<?> clazz : ClassUtil.getAllAssignedClass(IDescriptionInstaller.class)) {
                String packageName = clazz.getPackage().getName();
                //TODO:: 临时处理插件加载两遍
                if (packageName.contains("web") && !packageName.contains(".v2.")) {
                    LOGGER.debug("跳过V1的installer: {}", clazz);
                    continue;
                }
                Object installer = clazz.newInstance();
                installers.add((IDescriptionInstaller) installer);
            }
        } catch (Exception e) {
            LOGGER.warn("插件监听器初始化失败! ", e);
        }
        LOGGER.debug("installers: {}", installers);
    }

}
