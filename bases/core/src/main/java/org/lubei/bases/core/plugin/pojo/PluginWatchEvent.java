package org.lubei.bases.core.plugin.pojo;

import java.nio.file.WatchKey;

/**
 * 插件变化事件
 *
 * @author chengxingyu
 */
public class PluginWatchEvent {

    private String key;

    public PluginWatchEvent(WatchKey watchKey) {
        key = watchKey.toString();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
