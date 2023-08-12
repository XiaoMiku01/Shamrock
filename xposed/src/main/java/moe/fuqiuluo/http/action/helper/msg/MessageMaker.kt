package moe.fuqiuluo.http.action.helper.msg

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.xposed.tools.asStringOrNull

internal typealias IMaker = (JsonObject) -> MsgElement

internal object MessageMaker {
    private val makerArray = mutableMapOf(
        "text" to ::createTextElem
    )

     private fun createTextElem(data: JsonObject): MsgElement {
         val elem = MsgElement()
         elem.elementType = MsgConstant.KELEMTYPETEXT
         val text = TextElement()
         text.content = data["text"].asStringOrNull ?: "null"
         elem.textElement = text
         return elem
    }

    operator fun get(type: String): IMaker? = makerArray[type]
}