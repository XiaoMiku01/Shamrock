package moe.fuqiuluo.xposed.loader

import moe.protocol.service.PacketReceiver
import kotlin.reflect.jvm.jvmName

/**
 * 类交换中心，让目标应用可以访问模块内的类
 */
object FixedLoader: ClassLoader() {
    private val allowLoadedClass = arrayOf(
        PacketReceiver::class,
    )

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        allowLoadedClass.forEach {
            if (name == it.jvmName) {
                return it.java
            }
        }
        // FFmpeg库的类允许QQ访问
        if (name?.startsWith("com.arthenica.") == true) {
            return LuoClassloader.loadClass(name)
        }
        return super.loadClass(name, resolve)
    }
}