package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.xposed.loader.NativeLoader
import mqq.app.MobileQQ

internal class FixLibraryLoad: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformHelper.isMainProcess()) {
            return
        }
        XposedHelpers.findAndHookMethod(com.arthenica.ffmpegkit.NativeLoader::class.java,
            "loadLibrary", String::class.java,
            object: XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val name: String = param.args[0] as String
                    if (name == "ffmpegkit") {
                        arrayOf(
                            "avutil",
                            "swscale",
                            "swresample",
                            "avcodec",
                            "avformat",
                            "avfilter",
                            "avdevice"
                        ).forEach {
                            NativeLoader.load(it)
                        }
                    }
                    NativeLoader.load(name)
                    param.result = null
                }
            })

    }
}