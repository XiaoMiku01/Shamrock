package moe.fuqiuluo.xposed.loader

import android.content.Context
import moe.fuqiuluo.xposed.actions.NoBackGround
import moe.fuqiuluo.xposed.actions.InitRemoteService
import moe.fuqiuluo.xposed.actions.DataReceiver
import moe.fuqiuluo.xposed.actions.FetchService
import moe.fuqiuluo.xposed.actions.FixLibraryLoad
import moe.fuqiuluo.xposed.actions.ForceTablet
import moe.fuqiuluo.xposed.actions.HookForDebug
import moe.fuqiuluo.xposed.actions.HookWrapperCodec
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.actions.IpcService
import moe.fuqiuluo.xposed.actions.PullConfig
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object ActionLoader {
    private val ACTION_FIRST_LIST = arrayOf(
        DataReceiver::class, // 注册一个接收数据的动态广播
        PullConfig::class, // 从APP进程拉扯配置文件
        ForceTablet::class, // 强制平板模式
        HookWrapperCodec::class, // 注册服务处理器
        HookForDebug::class,
        IpcService::class,
        FixLibraryLoad::class
    )

    private val ACTION_LIST = arrayOf<KClass<*>>(
        FetchService::class, // 获取服务实例
        InitRemoteService::class, // 创建HTTP API
        NoBackGround::class, // 反QQ后台模式
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