package moe.fuqiuluo.http.action.helper.msg

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import moe.fuqiuluo.http.action.helper.ContactHelper
import moe.fuqiuluo.http.action.helper.HighwayHelper
import moe.fuqiuluo.http.action.helper.MessageHelper
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json

internal object MsgConvert {
    suspend fun convertMsgRecordToMsgSegment(record: MsgRecord, chatType: Int = record.chatType): ArrayList<HashMap<String, JsonElement>> {
        return convertMsgElementsToMsgSegment(chatType, record.elements)
    }

    suspend fun convertMsgElementsToMsgSegment(chatType: Int, elements: List<MsgElement>): ArrayList<HashMap<String, JsonElement>> {
        val messageData = arrayListOf<HashMap<String, JsonElement>>()
        elements.forEach {
            if (it.elementType == MsgConstant.KELEMTYPETEXT) {
                val text = it.textElement
                if (text.atType != MsgConstant.ATTYPEUNKNOWN) {
                    messageData.add(hashMapOf(
                        "type" to "at".json,
                        "data" to JsonObject(mapOf(
                            "qq" to ContactHelper.getUinByUid(text.atNtUid).json,
                        ))
                    ))
                } else {
                    messageData.add(hashMapOf(
                        "type" to "text".json,
                        "data" to JsonObject(mapOf(
                            "text" to text.content.json
                        ))
                    ))
                }
            } else if (it.elementType == MsgConstant.KELEMTYPEFACE) {
                val face = it.faceElement
                if (face.faceType == 5) {
                    messageData.add(hashMapOf(
                        "type" to "poke".json,
                        "data" to JsonObject(mapOf(
                            "type" to face.pokeType.json,
                            "id" to face.vaspokeId.json,
                            "strength" to face.pokeStrength.json
                        ))
                    ))
                }
                messageData.add(hashMapOf(
                    "type" to "face".json,
                    "data" to JsonObject(mapOf(
                        "id" to face.faceIndex.json
                    ))
                ))
            } else if (it.elementType == MsgConstant.KELEMTYPEPIC) {
                val image = it.picElement
                val md5 = image.md5HexStr
                messageData.add(hashMapOf(
                    "type" to "image".json,
                    "data" to JsonObject(mapOf(
                        "file" to md5.json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEC2C -> "http://gchat.qpic.cn/gchatpic_new/0/0-0-${md5.uppercase()}/0?term=2"
                            MsgConstant.KCHATTYPEGROUP -> "https://c2cpicdw.qpic.cn/offpic_new/0/${md5.uppercase()}/0?term=2"
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ))
                ))
            } else if (it.elementType == MsgConstant.KELEMTYPEPTT) {
                val record = it.pttElement

                val md5 = if (record.fileName.startsWith("silk"))
                    record.fileName.substring(5)
                else record.md5HexStr

                messageData.add(hashMapOf(
                    "type" to "record".json,
                    "data" to JsonObject(mapOf(
                        "file" to md5.json,
                        "magic" to (if(record.voiceChangeType == MsgConstant.KPTTVOICECHANGETYPENONE) "0" else "1").json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEGROUP -> HighwayHelper.requestDownGroupPtt("0", record.md5HexStr, record.fileUuid)
                            MsgConstant.KCHATTYPEC2C -> HighwayHelper.requestDownC2CPtt("0", record.fileUuid)
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ))
                ))
            } else if (it.elementType == MsgConstant.KELEMTYPEVIDEO) {
                val video = it.videoElement
                messageData.add(hashMapOf(
                    "type" to "video".json,
                    "data" to JsonObject(mapOf(
                        "file" to video.fileName.json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEGROUP -> HighwayHelper.requestDownGroupVideo("0", video.fileName, video.fileUuid)
                            MsgConstant.KCHATTYPEC2C -> HighwayHelper.requestDownC2CVideo("0", video.fileName, video.fileUuid)
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ))
                ))
            } else if (it.elementType == MsgConstant.KELEMTYPEMARKETFACE) {
                val face = it.marketFaceElement
                when (face.emojiId.lowercase()) {
                    "4823d3adb15df08014ce5d6796b76ee1" -> messageData.add(hashMapOf("type" to "dice".json))
                    "83c8a293ae65ca140f348120a77448ee" -> messageData.add(hashMapOf("type" to "rps".json))
                }
            } else if (it.elementType == MsgConstant.KELEMTYPEARKSTRUCT) {
                kotlin.runCatching {
                    val data = Json.parseToJsonElement(it.arkElement.bytesData).asJsonObject
                    when (data["view"].asString) {
                        "news" -> {
                            val info = data["meta"].asJsonObject["news"].asJsonObject
                            messageData.add(hashMapOf(
                                "type" to "share".json,
                                "data" to JsonObject(mapOf(
                                    "url" to info["jumpUrl"]!!,
                                    "title" to info["title"]!!,
                                    "content" to info["desc"]!!,
                                    "image" to info["preview"]!!
                                ))
                            ))
                        }
                        "LocationShare" -> {
                            val info = data["meta"].asJsonObject["Location.Search"].asJsonObject
                            messageData.add(hashMapOf(
                                "type" to "location".json,
                                "data" to JsonObject(mapOf(
                                    "lat" to info["lat"]!!,
                                    "lon" to info["lng"]!!,
                                    "content" to info["address"]!!,
                                    "title" to info["name"]!!
                                ))
                            ))
                        }
                        "contact" -> {
                            val info = data["meta"].asJsonObject["contact"].asJsonObject
                            if(data["app"].asString == "com.tencent.troopsharecard") {
                                messageData.add(hashMapOf(
                                    "type" to "contact".json,
                                    "data" to JsonObject(mapOf(
                                        "type" to "group".json,
                                        "id" to info["jumpUrl"].asString.split("group_code=")[1].json,
                                    ))
                                ))
                            } else {
                                messageData.add(hashMapOf(
                                    "type" to "contact".json,
                                    "data" to JsonObject(mapOf(
                                        "type" to "private".json,
                                        "id" to info["jumpUrl"].asString.split("uin=")[1].json,
                                    ))
                                ))
                            }
                        }
                        // "music" -> {}
                        else -> {
                            messageData.add(hashMapOf(
                                "type" to "json".json,
                                "data" to JsonObject(mapOf(
                                    "data" to it.arkElement.bytesData.json,
                                ))
                            ))
                        }
                    }
                }.onFailure {
                    LogCenter.log(it.stackTraceToString(), Level.ERROR)
                }
            } else if(it.elementType == MsgConstant.KELEMTYPEREPLY) {
                val reply = it.replyElement
                val msgId = reply.replayMsgId
                messageData.add(hashMapOf(
                    "type" to "reply".json,
                    "data" to JsonObject(mapOf(
                        "id" to MessageHelper.generateMsgIdHash(chatType, msgId).json,
                    ))
                ))
            } else {
                LogCenter.log("不支持的消息Elem转消息段: ${it.elementType}", Level.WARN)
            }
        }
        return messageData
    }

}