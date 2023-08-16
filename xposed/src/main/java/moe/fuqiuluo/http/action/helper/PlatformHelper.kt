package moe.fuqiuluo.http.action.helper

import android.content.Context
import android.content.pm.PackageInfo

internal object PlatformHelper {

    fun getClientVersion(context: Context): String {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName
        return "android $versionName"
    }

}