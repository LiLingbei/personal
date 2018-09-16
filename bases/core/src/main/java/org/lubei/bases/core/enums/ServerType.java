package org.lubei.bases.core.enums;

import com.google.common.base.Strings;


/** 系统服务器类型 ***/
public enum ServerType {
    DCS("dcs"), CCS("ccs"), PAS("pas"), EAC("eac"), SINGLE("single"), NFA("nfa"), LOG("log"), DB(
        "db"), MOCK_DATA("mock.data");

    /**
     * <code>id</code> - id.
     */
    private String id;

    public static ServerType parse(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        for (ServerType generic : ServerType.values()) {
            if (generic.getId().equals(id)) {
                return generic;
            }
        }
        return null;
    }

    /**
     * Constructors.
     * 
     * @param id 标识号 标识号
     */
    private ServerType(final String id) {
        this.id = id;
    }

    /**
     * @return 枚举值
     */
    public String getId() {
        return id;
    }
}
