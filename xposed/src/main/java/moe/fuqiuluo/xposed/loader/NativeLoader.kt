package moe.fuqiuluo.xposed.loader

import android.annotation.SuppressLint
import moe.fuqiuluo.xposed.helper.LogCenter
import mqq.app.MobileQQ
import java.io.File

internal object NativeLoader {
    /**
     * 使目标进程可以使用来自模块的库
     */
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    fun load(name: String) {
        val context = MobileQQ.getContext()
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo("moe.fuqiuluo.shamrock", 0)
        val file = File(applicationInfo.nativeLibraryDir)
        LogCenter.log("LoadLibrary(name = $name)")
        System.load(file.resolve("lib$name.so").absolutePath)
    }
}