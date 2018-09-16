package org.lubei.bases.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Modifier

/**
 * 伴生类开启SLF4J日志
 *
 * 标准用法 `companion object : KLogging()`
 */
open class KLogging {
    val logger: Logger = LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass))

    companion object {
        fun <T : Any> unwrapCompanionClass(clazz: Class<T>): Class<*> {
            if (clazz.enclosingClass != null) {
                try {
                    val field = clazz.enclosingClass.getField(clazz.simpleName)
                    if (Modifier.isStatic(field.modifiers) && field.type == clazz) {
                        // && field.get(null) === obj
                        // the above might be safer but problematic with initialization order
                        return clazz.enclosingClass
                    }
                } catch(e: Exception) {
                    //ok, it is not a companion object
                }
            }
            return clazz
        }

    }
}
