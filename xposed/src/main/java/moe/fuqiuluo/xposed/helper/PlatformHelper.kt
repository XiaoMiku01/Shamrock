package moe.fuqiuluo.xposed.helper

import android.content.Context
import android.content.pm.PackageInfo
import mqq.app.MobileQQ

internal object PlatformHelper {
    /**
     * 获取OIDB包的ClientVersion信息
     */
    fun getClientVersion(context: Context): String {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        return "android $versionName"
    }

    /**
     * 是否处于QQ MSF协议进程
     */
    fun isMsfProcess(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName.contains("msf", ignoreCase = true)
    }

    /**
     * 是否处于QQ主进程
     */
    fun isMainProcess(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName == "com.tencent.mobileqq"
    }
}