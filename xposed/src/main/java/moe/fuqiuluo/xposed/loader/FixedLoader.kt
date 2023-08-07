package moe.fuqiuluo.xposed.loader

import com.tencent.mobileqq.service.PacketReceiver

object FixedLoader: ClassLoader() {
    override fun loadClass(name: String?, resolve: Boolean): Class<*> {
        if (name == PacketReceiver::class.java.name) {
            return PacketReceiver::class.java
        }
        return super.loadClass(name, resolve)
    }
}