package moe.fuqiuluo.xposed.loader

import android.content.Context
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.actions.impl.PullConfig
import moe.fuqiuluo.xposed.actions.impl.DataReceiver
import kotlin.reflect.full.createInstance

object ActionLoader {
    private val ACTION_LIST = arrayOf(
        DataReceiver::class, // 注册一个接收数据的动态广播
        PullConfig::class, // 从APP进程拉扯配置文件
    )

    fun runAll(ctx: Context) {
        ACTION_LIST.forEach {
            val action = it.createInstance() as IAction
            action.invoke(ctx)
        }
    }
}