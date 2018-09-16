package org.lubei.bases.core.plugin.pf4j;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 插件配置.
 *
 * @author gaojuhua.
 */
public class PluginConfigBean {
    /**
     * 读取器
     */
    private List<String> readers;
    /**
     * 安装器
     */
    private List<String> installers;


    public PluginConfigBean() {
        this.readers = Lists.newArrayList();
        this.installers = Lists.newArrayList();
    }

    public List<String> getReaders() {
        return readers;
    }

    public void setReaders(List<String> readers) {
        this.readers = readers;
    }

    public List<String> getInstallers() {
        return installers;
    }

    public void setInstallers(List<String> installers) {
        this.installers = installers;
    }
}
