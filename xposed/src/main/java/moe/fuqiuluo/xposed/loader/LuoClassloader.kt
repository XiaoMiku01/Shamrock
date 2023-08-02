package moe.fuqiuluo.xposed.loader

object LuoClassloader: ClassLoader() {
    lateinit var hostClassLoader: ClassLoader

    fun load(name: String): Class<*>? {
        return kotlin.runCatching {
            loadClass(name)
        }.getOrNull()
    }

    override fun loadClass(name: String?): Class<*> {
        return kotlin.runCatching {
            hostClassLoader.loadClass(name)
        }.getOrElse {
            super.loadClass(name)
        }
    }
}