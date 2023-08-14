package moe.fuqiuluo.xposed.loader

import android.annotation.SuppressLint
import mqq.app.MobileQQ
import java.io.File

internal object NativeLoader {
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    fun load(name: String) {
        val context = MobileQQ.getContext()
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo("moe.fuqiuluo.shamrock", 0)
        val file = File(applicationInfo.nativeLibraryDir)
        System.load(file.resolve("lib$name.so").absolutePath)
    }
}