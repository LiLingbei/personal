package org.lubei.bases.core.annotation;

/**
 * 注解用布尔类型
 */
public enum BoolType {
    FALSE(Boolean.FALSE),
    TRUE(Boolean.TRUE),
    NONE(null);

    private Boolean value;

    BoolType(Boolean value) {
        this.value = value;
    }

    public static BoolType from(Boolean value) {
        if (value == null) {
            return NONE;
        }
        return value.booleanValue() ? TRUE : FALSE;
    }

    public Boolean getValue() {
        return value;
    }
}
