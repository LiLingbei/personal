package org.lubei.bases.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audit {

    /**
     * 模块
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 类型
     *
     * @return 类型
     */
    AuditType type() default AuditType.NONE;

    /**
     * 操作名称
     *
     * @return 操作名称
     */
    String name() default "";

    /**
     * 请求信息表达式（JXLT）
     *
     * @return 请求信息表达式
     */
    String request() default "";

    /**
     * 响应信息表达式（JXLT）
     *
     * @return 响应信息表达式
     */
    String response() default "";

    /**
     * 不需要登录即可调用
     */
    BoolType noLogin() default BoolType.NONE;
}
