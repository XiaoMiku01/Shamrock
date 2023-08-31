package com.tencent.qqnt.msg

import com.tencent.qqnt.helper.ContactHelper
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import com.tencent.qqnt.transfile.RichProtoSvc
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.asJsonObject
import moe.fuqiuluo.xposed.tools.asString
import moe.fuqiuluo.xposed.tools.json

internal suspend fun MsgRecord.toSegment(): ArrayList<HashMap<String, JsonElement>> {
    return MsgConvert.convertMsgRecordToMsgSegment(this)
}

internal suspend fun MsgRecord.toCQCode(): String {
    return MsgConvert.convertMsgRecordToCQCode(this)
}

internal suspend fun List<MsgElement>.toSegment(chatType: Int): ArrayList<HashMap<String, JsonElement>> {
    return MsgConvert.convertMsgElementsToMsgSegment(chatType, this)
}

internal suspend fun List<MsgElement>.toCQCode(chatType: Int): String {
    return MsgConvert.convertMsgElementsToCQCode(this, chatType)
}

internal object MsgConvert {
    suspend fun convertMsgRecordToCQCode(record: MsgRecord, chatType: Int = record.chatType): String {
        return MessageHelper.encodeCQCode(convertMsgElementsToMsgSegment(chatType, record.elements))
    }

    suspend fun convertMsgElementsToCQCode(elements: List<MsgElement>, chatType: Int): String {
        return MessageHelper.encodeCQCode(convertMsgElementsToMsgSegment(chatType, elements))
    }

    suspend fun convertMsgRecordToMsgSegment(record: MsgRecord, chatType: Int = record.chatType): ArrayList<HashMap<String, JsonElement>> {
        return convertMsgElementsToMsgSegment(chatType, record.elements)
    }

    suspend fun convertMsgElementsToMsgSegment(chatType: Int, elements: List<MsgElement>): ArrayList<HashMap<String, JsonElement>> {
        val messageData = arrayListOf<HashMap<String, JsonElement>>()
        elements.forEach {
            try {
                val segment = covertMsgElementToMsgSegment(chatType, it)
                if (segment != null) {
                    messageData.add(segment)
                }
            } catch (e: Throwable) {
                LogCenter.log("消息element转换错误：$e", Level.WARN)
            }
        }
        return messageData
    }

    suspend fun covertMsgElementToMsgSegment(chatType: Int, element: MsgElement): HashMap<String, JsonElement>? {
        when (element.elementType) {
            MsgConstant.KELEMTYPETEXT -> {
                val text = element.textElement
                return if (text.atType != MsgConstant.ATTYPEUNKNOWN) {
                    hashMapOf(
                        "type" to "at".json,
                        "data" to JsonObject(mapOf(
                            "qq" to ContactHelper.getUinByUidAsync(text.atNtUid).json,
                        ))
                    )
                } else {
                    hashMapOf(
                        "type" to "text".json,
                        "data" to JsonObject(mapOf(
                            "text" to text.content.json
                        ))
                    )
                }
            }
            MsgConstant.KELEMTYPEFACE -> {
                val face = element.faceElement
                if (face.faceType == 5) {
                    return hashMapOf(
                        "type" to "poke".json,
                        "data" to JsonObject(mapOf(
                            "type" to face.pokeType.json,
                            "id" to face.vaspokeId.json,
                            "strength" to face.pokeStrength.json
                        ))
                    )
                }
                return hashMapOf(
                    "type" to "face".json,
                    "data" to JsonObject(mapOf(
                        "id" to face.faceIndex.json
                    ))
                )
            }
            MsgConstant.KELEMTYPEPIC -> {
                val image = element.picElement
                val md5 = image.md5HexStr
                return hashMapOf(
                    "type" to "image".json,
                    "data" to JsonObject(mapOf(
                        "file" to md5.json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEGROUP -> "http://gchat.qpic.cn/gchatpic_new/0/0-0-${md5.uppercase()}/0?term=2"
                            MsgConstant.KCHATTYPEC2C -> "https://c2cpicdw.qpic.cn/offpic_new/0/123-0-${md5.uppercase()}/0?term=2"
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ))
                )
            }
            MsgConstant.KELEMTYPEPTT -> {
                val record = element.pttElement

                val md5 = if (record.fileName.startsWith("silk"))
                    record.fileName.substring(5)
                else record.md5HexStr

                return hashMapOf(
                    "type" to "record".json,
                    "data" to JsonObject(hashMapOf(
                        "file" to md5.json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEGROUP -> RichProtoSvc.getGroupPttDownUrl("0", record.md5HexStr, record.fileUuid)
                            MsgConstant.KCHATTYPEC2C -> RichProtoSvc.getC2CPttDownUrl("0", record.fileUuid)
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ).also {
                        if(record.voiceChangeType != MsgConstant.KPTTVOICECHANGETYPENONE) {
                            it["magic"] = "1".json
                        }
                        if (it["url"].asString.isBlank()) {
                            it.remove("url")
                        }
                    })
                )
            }
            MsgConstant.KELEMTYPEVIDEO -> {
                val video = element.videoElement
                return hashMapOf(
                    "type" to "video".json,
                    "data" to JsonObject(hashMapOf(
                        "file" to video.fileName.json,
                        "url" to when(chatType) {
                            MsgConstant.KCHATTYPEGROUP -> RichProtoSvc.getGroupVideoDownUrl("0", video.fileName, video.fileUuid)
                            MsgConstant.KCHATTYPEC2C -> RichProtoSvc.getC2CVideoDownUrl("0", video.fileName, video.fileUuid)
                            else -> error("Not supported chat type: $chatType, convertMsgElementsToMsgSegment::Pic")
                        }.json
                    ).also {
                        if (it["url"].asString.isBlank()) {
                            it.remove("url")
                        }
                    })
                )
            }
            MsgConstant.KELEMTYPEMARKETFACE -> {
                val face = element.marketFaceElement
                when (face.emojiId.lowercase()) {
                    "4823d3adb15df08014ce5d6796b76ee1" -> return hashMapOf("type" to "dice".json)
                    "83c8a293ae65ca140f348120a77448ee" -> return hashMapOf("type" to "rps".json)
                }
            }
            MsgConstant.KELEMTYPEARKSTRUCT -> {
                kotlin.runCatching {
                    val data = Json.parseToJsonElement(element.arkElement.bytesData).asJsonObject
                    when (data["view"].asString) {
                        "news" -> {
                            val info = data["meta"].asJsonObject["news"].asJsonObject
                            return hashMapOf(
                                "type" to "share".json,
                                "data" to JsonObject(mapOf(
                                    "url" to info["jumpUrl"]!!,
                                    "title" to info["title"]!!,
                                    "content" to info["desc"]!!,
                                    "image" to info["preview"]!!
                                ))
                            )
                        }
                        "LocationShare" -> {
                            val info = data["meta"].asJsonObject["Location.Search"].asJsonObject
                            return hashMapOf(
                                "type" to "location".json,
                                "data" to JsonObject(mapOf(
                                    "lat" to info["lat"]!!,
                                    "lon" to info["lng"]!!,
                                    "content" to info["address"]!!,
                                    "title" to info["name"]!!
                                ))
                            )
                        }
                        "contact" -> {
                            val info = data["meta"].asJsonObject["contact"].asJsonObject
                            val packageName = data["app"].asString
                            if(packageName == "com.tencent.troopsharecard") {
                                return hashMapOf(
                                    "type" to "contact".json,
                                    "data" to JsonObject(mapOf(
                                        "type" to "group".json,
                                        "id" to info["jumpUrl"].asString.split("group_code=")[1].json,
                                    ))
                                )
                            //} else if (packageName == "com.tencent.multimsg") {
                            } else if (packageName == "com.tencent.contact.lua") {
                                return hashMapOf(
                                    "type" to "contact".json,
                                    "data" to JsonObject(mapOf(
                                        "type" to "private".json,
                                        "id" to info["jumpUrl"].asString.split("uin=")[1].json,
                                    ))
                                )
                            } else {
                                LogCenter.log("Not supported xml app: $packageName", Level.WARN)
                                return hashMapOf(
                                    "type" to "json".json,
                                    "data" to JsonObject(mapOf(
                                        "data" to element.arkElement.bytesData.json,
                                    ))
                                )
                            }
                        }
                        // "music" -> {}
                        else -> {
                            return hashMapOf(
                                "type" to "json".json,
                                "data" to JsonObject(mapOf(
                                    "data" to element.arkElement.bytesData.json,
                                ))
                            )
                        }
                    }
                }.onFailure {
                    LogCenter.log(it.stackTraceToString(), Level.ERROR)
                }
            }
            MsgConstant.KELEMTYPEREPLY -> {
                val reply = element.replyElement
                val msgId = reply.replayMsgId
                return hashMapOf(
                    "type" to "reply".json,
                    "data" to JsonObject(mapOf(
                        "id" to MessageHelper.generateMsgIdHash(chatType, msgId).json,
                    ))
                )
            }
            else -> {
                LogCenter.log("不支持的消息Elem转消息段: ${element.elementType}", Level.WARN)
            }
        }
        return null
    }

}