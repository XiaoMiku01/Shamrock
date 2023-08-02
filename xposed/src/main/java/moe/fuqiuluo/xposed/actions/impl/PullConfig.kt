package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.DataRequester

import de.robv.android.xposed.XposedBridge.log
import moe.fuqiuluo.xposed.loader.ActionLoader
import mqq.app.MobileQQ
import kotlin.concurrent.thread
import kotlin.system.exitProcess

var isConfigOk = false

class PullConfig: IAction {
    override fun invoke(ctx: Context) {
        if (MobileQQ.getMobileQQ().qqProcessName != "com.tencent.mobileqq") return

        DataRequester.request(ctx, "init", bodyBuilder = {}, onFailure = {
            GlobalUi.post {
                Toast.makeText(ctx, "请启动Shamrock主进程以初始化服务，进程将退出。", Toast.LENGTH_LONG).show()
            }
            thread {
                Thread.sleep(3000)
                exitProcess(1)
            }
        }) {
            isConfigOk = true
            log("接到APP返回")
            // do something

            val preferences = ctx.getSharedPreferences("shamrock_config", 0)
            preferences.edit {
                putBoolean("tablet", it.getBooleanExtra("tablet", false))
            }

            ActionLoader.runService(ctx)
        }
    }
}