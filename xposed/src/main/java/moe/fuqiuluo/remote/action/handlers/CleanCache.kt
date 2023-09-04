package moe.fuqiuluo.remote.action.handlers

import moe.protocol.servlet.utils.FileUtils
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.utils.MMKVFetcher

internal object CleanCache: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        return invoke()
    }

    operator fun invoke(): String {
        FileUtils.clearCache()
        MMKVFetcher.mmkvWithId("shamrock")
            .clear()
        return ok("成功")
    }

    override fun path(): String = "clean_cache"
}