package moe.fuqiuluo.xposed.loader

import com.tencent.mobileqq.service.PacketReceiver
import com.tencent.mobileqq.service.ProfileProcessor

object FixedLoader: ClassLoader() {
    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        if (name == PacketReceiver::class.java.name) {
            return PacketReceiver::class.java
        } else if (name == ProfileProcessor::class.java.name) {
            return ProfileProcessor::class.java
        }
        return super.loadClass(name, resolve)
    }
}