package org.lubei.bases.core.annotation;

/**
 * 服务自动注册类注解,该注解表示被注解的服务不需要自动注册启动。
 *
 * @Author zlzhang Created by sany on 2015/11/4.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务自动注册类注解,该注解表示被注解的服务不需要自动注册启动。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcludeAutoWare {

}
