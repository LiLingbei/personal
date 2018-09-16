package org.lubei.bases.core.annotation;

/**
 * 审计类型
 */
public enum AuditType {
    NONE(""),
    CREATE("创建"),
    READ("读取"),
    UPDATE("更新"),
    DELETE("删除"),
    OTHER("其它");

    /**
     * 标签
     */
    String label;

    AuditType(String label) {
        this.label = label;
    }

    /**
     * 获取显示标签
     *
     * @return 标签
     */
    public String getLabel() {
        return label;
    }

}
