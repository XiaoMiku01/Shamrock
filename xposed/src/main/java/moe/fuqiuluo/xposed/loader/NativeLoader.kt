package moe.fuqiuluo.xposed.loader

import android.annotation.SuppressLint
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import mqq.app.MobileQQ
import java.io.File

internal object NativeLoader {
    private val externalLibPath = MobileQQ.getContext()
        .getExternalFilesDir(null)!!.parentFile!!.resolve("Tencent/Shamrock/lib")

    val isVoiceLoaded: Boolean
        get() {
            return externalLibPath.resolve("libffmpegkit.so").exists()
        }

    /**
     * 使目标进程可以使用来自模块的库
     */
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    fun load(name: String) {
        try {
            val context = MobileQQ.getContext()
            if (name == "shamrock" || name == "xposed") {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo("moe.fuqiuluo.shamrock", 0)
                val file = File(applicationInfo.nativeLibraryDir)
                LogCenter.log("LoadLibrary(name = $name)")
                System.load(file.resolve("lib$name.so").absolutePath)
            } else {
                val sourceFile = externalLibPath.resolve("lib$name.so")
                val soFile = MobileQQ.getContext().filesDir.parentFile!!.resolve("txlib").resolve("lib$name.so")
                if (!soFile.exists()) {
                    if (!sourceFile.exists()) {
                        LogCenter.log("LoadExternalLibrary(name = $name) failed, file not exists.", level = Level.ERROR)
                        return
                    } else {
                        sourceFile.copyTo(soFile)
                    }
                }
                System.load(soFile.absolutePath)
                LogCenter.log("LoadExternalLibrary(name = $name)")
            }
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.ERROR)
        }
    }
}