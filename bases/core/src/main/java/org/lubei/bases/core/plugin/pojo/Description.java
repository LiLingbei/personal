package org.lubei.bases.core.plugin.pojo;

import java.io.Serializable;

/**
 * 插件描述
 *
 * @author gaojuhua
 */

public abstract class Description implements Serializable {

    private static final long serialVersionUID = 6828194505655014153L;
    /**
     * 描述类型
     */
    private String type;
    /**
     * 插件名称
     */
    private String plugin;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
}
