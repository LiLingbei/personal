package org.lubei.bases.common.plugin;

import static org.lubei.bases.core.service.ServicesFactory.registerService;

import org.lubei.bases.core.plugin.IPluginDescriptionService;
import org.lubei.bases.core.plugin.IPluginService;
import org.lubei.bases.core.plugin.pf4j.PluginDescriptionService;
import org.lubei.bases.core.plugin.pf4j.PluginListener;
import org.lubei.bases.core.plugin.pf4j.PluginService;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件初始化服务
 *
 * @author gaojuhua
 */
public class PluginDaemon extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginDaemon.class);

    @Override
    protected void doStart() {
        LOGGER.info("开始启动插件Daemon");
        try {
            init();
            notifyStarted();
        } catch (Exception e) {
            LOGGER.error("启动插件Daemon失败!", e);
            Throwables.propagate(e);
        }
    }

    @Override
    protected void doStop() {

    }

    /**
     * 初始化插件服务
     */
    private void init() {
        registerService(IPluginDescriptionService.class, new PluginDescriptionService());
        //
        IPluginService pluginService = new PluginService();
        pluginService.addListener(new PluginListener());
        //注册插件服务
        registerService(IPluginService.class, pluginService);
        pluginService.init();


    }
}
