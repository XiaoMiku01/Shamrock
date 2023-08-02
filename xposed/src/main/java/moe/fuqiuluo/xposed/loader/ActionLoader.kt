package moe.fuqiuluo.xposed.loader

import android.content.Context
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.actions.impl.PullConfig
import moe.fuqiuluo.xposed.actions.impl.DataReceiver
import moe.fuqiuluo.xposed.actions.impl.FetchService
import moe.fuqiuluo.xposed.actions.impl.ForceTablet
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object ActionLoader {
    private val ACTION_FIRST_LIST = arrayOf(
        DataReceiver::class, // 注册一个接收数据的动态广播
        PullConfig::class, // 从APP进程拉扯配置文件
    )

    private val ACTION_LIST = arrayOf<KClass<*>>(
        ForceTablet::class, // 强制平板模式
        FetchService::class, // 获取服务实例

    )

    // 先从APP拉取配置文件，再执行其他操作
    fun runFirst(ctx: Context) {
        ACTION_FIRST_LIST.forEach {
            val action = it.createInstance()
            action.invoke(ctx)
        }
    }

    fun runService(ctx: Context) {
        ACTION_LIST.forEach {
            if (it.java != DataReceiver::class.java) {
                val action = it.createInstance() as IAction
                action.invoke(ctx)
            }
        }
    }
}