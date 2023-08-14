package moe.fuqiuluo.xposed.loader

import com.tencent.mobileqq.service.PacketReceiver
import com.tencent.mobileqq.service.ProfileProcessor
import moe.fuqiuluo.http.action.helper.codec.SilkProcessor
import kotlin.reflect.jvm.jvmName

object FixedLoader: ClassLoader() {
    private val allowLoadedClass = arrayOf(
        PacketReceiver::class,
        ProfileProcessor::class,
        SilkProcessor::class
    )

    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        allowLoadedClass.forEach {
            if (name == it.jvmName) {
                return it.java
            }
        }
        if (name?.startsWith("com.arthenica.") == true) {
            return LuoClassloader.loadClass(name)
        }
        return super.loadClass(name, resolve)
    }
}