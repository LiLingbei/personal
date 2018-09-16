package org.lubei.bases.core.plugin.pf4j;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import org.lubei.bases.core.plugin.pojo.PluginWatchEvent;
import org.lubei.bases.core.util.GlobalEventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 插件变化监视器.
 *
 * @author gaojuhua.
 */
public class PluginWatcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginWatcher.class);
    /**
     * 监控路径
     */
    private Path path;
    /**
     * 监听服务
     */
    private WatchService watchService;
    /**
     * 插件服务
     */
    private PluginService pluginService;

    /**
     * 构造方法
     */
    public PluginWatcher(PluginService pluginService) {
        this.pluginService = pluginService;
        try {
            path = Paths.get(System.getProperty(PluginService.PF4J_DIR));
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, ENTRY_CREATE, ENTRY_DELETE);
        } catch (IOException e) {
            LOGGER.warn("插件目录监听初始化失败!", e);
        }
    }

    /**
     * 主线程
     */
    public void run() {

        //
        WatchKey key;
        while (true) {
            try {
                key = watchService.poll(5, TimeUnit.MINUTES);
                if (key == null) {
                    pluginService.syncPlugins();
                } else {
                    //重新加载插件列表
                    reloadPlugins(key);
                }
                //发插件变化事件到总线
                GlobalEventBus.INSTANCE.post(new PluginWatchEvent(key));
            } catch (Throwable e) {
                LOGGER.warn("插件目录监听发生异常!", e);
            }
        }
    }

    /**
     * 重新加载插件
     */
    private void reloadPlugins(WatchKey key) {
        //
        List<WatchEvent<?>> events = key.pollEvents();
        for (WatchEvent event : events) {
            WatchEvent.Kind kind = event.kind();
            LOGGER.trace("插件目录发生事件  {}, {} ", event.context(), kind);
        }
        //重置
        boolean reset = key.reset();
        if (!reset) {
            LOGGER.warn("插件目录监听停止!");
        }
        pluginService.loadPlugins();
        pluginService.syncPlugins();
    }
}
