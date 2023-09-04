package moe.fuqiuluo.remote.action.handlers

import moe.protocol.service.data.OutResource
import moe.protocol.servlet.helper.LocalCacheHelper
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.utils.AudioUtils

internal object GetRecord: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val file = session.getString("file")
            .replace(regex = "[{}\\-]".toRegex(), replacement = "")
            .replace(" ", "")
            .split(".")[0].lowercase()
        val format = session.getString("out_format")
        return invoke(file, format)
    }

    operator fun invoke(file: String, format: String): String {
        val pttFile = LocalCacheHelper.getCachePttFile(file)
        return if(pttFile.exists()) {
            val isSilk = AudioUtils.isSilk(pttFile)
            val audioFile = when(format) {
                "amr" -> AudioUtils.audioToAmr(pttFile, isSilk)
                else -> AudioUtils.audioToFormat(pttFile, isSilk, format)
            }
            ok(
                OutResource(
                audioFile.toString(),
                url = "/res/${audioFile.nameWithoutExtension}"
            )
            )
        } else {
            error("not found record file from cache")
        }
    }

    override val requiredParams: Array<String> = arrayOf("file", "out_format")

    override fun path(): String = "get_record"
}