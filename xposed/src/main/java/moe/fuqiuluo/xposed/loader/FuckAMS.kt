@file:Suppress("UNUSED_VARIABLE", "LocalVariableName")
package moe.fuqiuluo.xposed.loader

import android.content.pm.ApplicationInfo
import android.os.Build
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import moe.fuqiuluo.xposed.tools.hookMethod
import java.lang.reflect.Method

internal object FuckAMS {
    private val KeepPackage = arrayOf(
        "com.tencent.mobileqq", "moe.fuqiuluo.shamrock"
    )
    private val KeepRecords = arrayListOf<Any>()
    private lateinit var KeepThread: Thread

    private lateinit var METHOD_IS_KILLED: Method

    fun injectAMS(loader: ClassLoader) {
        kotlin.runCatching {
            val ActivityManagerService = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", loader)
            ActivityManagerService.hookMethod("newProcessRecordLocked").after {
                increaseAdj(it.result)
            }
        }.onFailure {
            XposedBridge.log("Plan A failed: ${it.message}")
        }
        kotlin.runCatching {
            val ProcessList = XposedHelpers.findClass("com.android.server.am.ProcessList", loader)
            ProcessList.hookMethod("newProcessRecordLocked").after {
                increaseAdj(it.result)
            }
        }.onFailure {
            XposedBridge.log("Plan B failed: ${it.message}")
        }
    }

    private fun checkThread() {
        if (!::KeepThread.isInitialized || !KeepThread.isAlive) {
            KeepThread = Thread {
                while (true) {
                    Thread.sleep(1000)
                    KeepRecords.forEach {
                        val isKilled = if (::METHOD_IS_KILLED.isInitialized) {
                            kotlin.runCatching {
                                 METHOD_IS_KILLED.invoke(it) as Boolean
                            }.getOrElse { false }
                        } else false
                        if (isKilled) {
                            KeepRecords.remove(it)
                        } else {
                            keepByAdj(it)
                        }
                    }
                }
            }.also {
                it.isDaemon = true
                it.start()
            }
        }
    }

    private fun increaseAdj(record: Any) {
        if (record.toString().contains("system", ignoreCase = true)) {
            return
        }
        val applicationInfo = record.javaClass.getDeclaredField("info").also {
            if (!it.isAccessible) it.isAccessible = true
        }.get(record) as ApplicationInfo
        if(applicationInfo.processName in KeepPackage) {
            KeepRecords.add(record)
            keepByAdj(record)
            keepByPersistent(record)
            checkThread()
        }
    }

    private fun keepByAdj(record: Any) {
        val clazz = record.javaClass
        if (!::METHOD_IS_KILLED.isInitialized) {
            kotlin.runCatching {
                METHOD_IS_KILLED = clazz.getDeclaredMethod("isKilled").also {
                    if (!it.isAccessible) it.isAccessible = true
                }
            }
        }
        kotlin.runCatching {
            val newState = clazz.getDeclaredField("mState").also {
                if (!it.isAccessible) it.isAccessible = true
            }.get(record)
            val MethodSetMaxAdj = newState.javaClass.getDeclaredMethod("setMaxAdj", Int::class.java).also {
                if (!it.isAccessible) it.isAccessible = true
            }.invoke(newState, 0)
        }.onFailure {
            clazz.getDeclaredField("maxAdj").also {
                if (!it.isAccessible) it.isAccessible = true
            }.set(record, 0)
        }
    }

    private fun keepByPersistent(record: Any) {
        val clazz = record.javaClass
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT < 29) {
                clazz.getDeclaredField("persistent").also {
                    if (!it.isAccessible) it.isAccessible = true
                }.set(record, true)
            } else {
                clazz.getDeclaredMethod("setPersistent", Boolean::class.java).also {
                    if (!it.isAccessible) it.isAccessible = true
                }.invoke(record, true)
            }
        }
    }
}