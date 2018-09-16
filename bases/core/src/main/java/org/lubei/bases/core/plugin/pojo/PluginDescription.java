package org.lubei.bases.core.plugin.pojo;

import java.util.Map;

/**
 * 插件描述.
 *
 * @author gaojuhua.
 */
public class PluginDescription extends MapDescription {

    public static final String PLUGIN = "plugin";
    private static final String NAME = "name";
    private static final String GROUP = "group";
    private static final String VERSION = "version";
    private static final String PROVIDER = "provider";
    private static final String DESCRIPTION = "description";
    private static final long serialVersionUID = -2942260899728649196L;

    public PluginDescription() {
        this.setType(PLUGIN);
    }

    public PluginDescription(Map<String, Object> attributes) {
        super(attributes);
        this.setType(PLUGIN);
        this.setPlugin(String.valueOf(super.get(NAME)));
    }

    public String getName() {
        return String.valueOf(super.get(NAME));
    }

    public void setName(String name) {
        super.put(NAME, name);
    }


    public String getGroup() {
        return String.valueOf(super.get(GROUP));
    }

    public void setGroup(String group) {
        super.put(GROUP, group);
    }


    public String getVersion() {
        return String.valueOf(super.get(VERSION));
    }

    public void setVersion(String version) {
        super.put(VERSION, version);
    }


    public String getProvider() {
        return String.valueOf(super.get(PROVIDER));
    }

    public void setProvider(String provider) {
        super.put(PROVIDER, provider);
    }


    public String getDescription() {
        return String.valueOf(super.get(DESCRIPTION));
    }

    public void setDescription(String description) {
        super.put(DESCRIPTION, description);
    }


}
