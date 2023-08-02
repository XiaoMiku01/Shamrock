package moe.fuqiuluo.xposed.tools

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XCallback
import moe.fuqiuluo.xposed.loader.LuoClassloader
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

fun beforeHook(ver: Int = XCallback.PRIORITY_DEFAULT, block: (param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHook {
    return object :XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            block(param)
        }
    }
}

fun afterHook(ver: Int = XCallback.PRIORITY_DEFAULT, block: (param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHook {
    return object :XC_MethodHook(ver) {
        override fun afterHookedMethod(param: MethodHookParam) {
            block(param)
        }
    }
}

object FuzzySearchClass {
    /**
     * QQ混淆字典
     */
    private val dic = arrayOf(
        "r" , "t", "o", "a", "b", "c", "e", "f", "d", "g", "h", "i", "j", "k", "l", "m", "n", "p", "q", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    /**
     * 通过特殊字段寻找类
     */
    fun findClassByField(prefix: String, check: (Field) -> Boolean): Class<*>? {
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix.$className")
            clz?.fields?.forEach {
                if (it.modifiers and Modifier.STATIC != 0
                    && !isBaseType(it.type)
                    && check(it)
                ) return clz
            }
        }
        return null
    }

    fun findAllClassByField(classLoader: ClassLoader, prefix: String, check: (String, Field) -> Boolean): List<Class<*>> {
        val list = arrayListOf<Class<*>>()
        dic.forEach { className ->
            kotlin.runCatching {
                val clz = classLoader.loadClass("$prefix.$className")
                clz.declaredFields.forEach {
                    if (!isBaseType(it.type) && check(className, it)) {
                        list.add(clz)
                    }
                }
            }
        }
        return list
    }

    fun findAllClassByMethod(classLoader: ClassLoader, prefix: String, check: (String, Method) -> Boolean): List<Class<*>> {
        val list = arrayListOf<Class<*>>()
        dic.forEach { className ->
            kotlin.runCatching {
                val clz = classLoader.loadClass("$prefix.$className")
                clz.declaredMethods.forEach {
                    if (check(className, it)) {
                        list.add(clz)
                    }
                }
            }
        }
        return list
    }

    fun findAllClassByField(prefix: String, check: (String, Field) -> Boolean): List<Class<*>> {
        val list = arrayListOf<Class<*>>()
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix.$className")
            clz?.fields?.forEach {
                if (!isBaseType(it.type)
                    && check(className, it)
                ) list.add(clz)
            }
        }
        return list
    }

    fun findClassByMethod(prefix: String, check: (Class<*>, Method) -> Boolean): Class<*>? {
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix.$className")
            clz?.methods?.forEach {
                if (check(clz, it)) return clz
            }
        }
        return null
    }

    private fun isBaseType(clz: Class<*>): Boolean {
        if (
            clz == Long::class.java ||
            clz == Double::class.java ||
            clz == Float::class.java ||
            clz == Int::class.java ||
            clz == Short::class.java ||
            clz == Char::class.java ||
            clz == Byte::class.java
        ) {
            return true
        }
        return false
    }
}