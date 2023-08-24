package moe.fuqiuluo.xposed.actions

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import com.tencent.qqnt.utils.PlatformUtils
import moe.fuqiuluo.xposed.loader.NativeLoader

internal class FixLibraryLoad: IAction {
    override fun invoke(ctx: Context) {
        if (!PlatformUtils.isMainProcess()) {
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