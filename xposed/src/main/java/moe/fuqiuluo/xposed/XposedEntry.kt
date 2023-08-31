package moe.fuqiuluo.xposed

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.XposedBridge.log
import moe.fuqiuluo.xposed.loader.ActionLoader
import moe.fuqiuluo.xposed.loader.FixedLoader
import moe.fuqiuluo.xposed.loader.FuckAMS
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.fuqiuluo.xposed.tools.FuzzySearchClass
import moe.fuqiuluo.xposed.tools.afterHook
import mqq.app.MobileQQ
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class XposedEntry: IXposedHookLoadPackage {
    companion object {
        const val PACKAGE_NAME_QQ = "com.tencent.mobileqq"
        const val PACKAGE_NAME_QQ_INTERNATIONAL = "com.tencent.mobileqqi"
        const val PACKAGE_NAME_QQ_LITE = "com.tencent.qqlite"
        const val PACKAGE_NAME_TIM = "com.tencent.tim"

        @JvmStatic
        var sec_static_stage_inited = false
    }

    private var firstStageInit = false

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName == PACKAGE_NAME_QQ) {
            entryMQQ(param.classLoader)
        } else if (param.packageName == "android") {
            FuckAMS.injectAMS(param.classLoader)
        }
    }

    private fun entryMQQ(classLoader: ClassLoader) {
        val startup = afterHook(51) { param ->
            try {
                val clz = param.thisObject.javaClass.classLoader!!
                    .loadClass("com.tencent.common.app.BaseApplicationImpl")
                val field = clz.declaredFields.first {
                    it.type == clz
                }
                val app: Context? = field.get(null) as? Context
                if (app != null) {
                    execStartupInit(app)
                }
            } catch (e: Throwable) {
                log(e)
            }
        }

        kotlin.runCatching {
            val loadDex = classLoader.loadClass("com.tencent.mobileqq.startup.step.LoadDex")
            loadDex.declaredMethods
                .filter { it.returnType.equals(java.lang.Boolean.TYPE) && it.parameterTypes.isEmpty() }
                .forEach {
                    XposedBridge.hookMethod(it, startup)
                }
            firstStageInit = true
        }.onFailure {
            // For NT QQ
            val fieldList = arrayListOf<Field>()
            FuzzySearchClass.findAllClassByField(classLoader, "com.tencent.mobileqq.startup.task.config") { _, field ->
                (field.type == HashMap::class.java || field.type == Map::class.java) && Modifier.isStatic(field.modifiers)
            }.forEach {
                it.declaredFields.forEach { field ->
                    if ((field.type == HashMap::class.java || field.type == Map::class.java)
                        && Modifier.isStatic(field.modifiers))
                        fieldList.add(field)
                }
            }
            fieldList.forEach {
                if (!it.isAccessible) it.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                (it.get(null) as? Map<String, Class<*>>).also { map ->
                    if (map == null) log("Not found matched entry")
                    else map.forEach { (key, clazz) ->
                        if (key.contains("LoadDex", ignoreCase = true)) {
                            clazz.declaredMethods.forEach {
                                if (it.parameterTypes.size == 1 && it.parameterTypes[0] == Context::class.java) {
                                    log("Try load fetchEntry's injector.")
                                    XposedBridge.hookMethod(it, startup)
                                }
                            }
                        }
                    }
                }
            }
            firstStageInit = true
        }
    }

    private fun execStartupInit(ctx: Context) {
        if (sec_static_stage_inited) return
        val classLoader = ctx.classLoader.also { requireNotNull(it) }
        if ("1" != System.getProperty("qxbot_flag")) {
            System.setProperty("qxbot_flag", "1")
        } else return

        LuoClassloader.hostClassLoader = classLoader
        injectClassloader(XposedEntry::class.java.classLoader)

        log("Process Name = " + MobileQQ.getMobileQQ().qqProcessName.apply {
            // if (!contains("msf", ignoreCase = true)) return // 非MSF进程 退出
        })

        // MSG LISTENER 进程运行在主进程
        // API 也应该开放在主进程

        sec_static_stage_inited = true

        ActionLoader.runFirst(ctx)
    }

    private fun injectClassloader(classLoader: ClassLoader?) {
        if (classLoader != null) {
            val parent = classLoader.parent
            val field = ClassLoader::class.java.declaredFields
                .first { it.name == "parent" }
            field.isAccessible = true
            field.set(LuoClassloader, parent)
            field.set(classLoader, LuoClassloader)

            val qloader = LuoClassloader.hostClassLoader
            val qparent = qloader.parent
            field.set(FixedLoader, qparent)
            field.set(qloader, FixedLoader)

            log("Classloader inject successfully.")
        }
    }
}