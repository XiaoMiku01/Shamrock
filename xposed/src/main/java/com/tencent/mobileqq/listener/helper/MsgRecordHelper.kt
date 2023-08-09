package com.tencent.mobileqq.listener.helper

import com.tencent.mobileqq.listener.entries.Message
import com.tencent.mobileqq.listener.entries.MessageChain
import com.tencent.mobileqq.listener.entries.MessageChainContent
import com.tencent.mobileqq.listener.entries.MessageChainType
import com.tencent.mobileqq.listener.entries.MessageType
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.ArrayList

internal object MsgRecordHelper {
    private val jsonEncoder = Json {
        serializersModule = SerializersModule {
            polymorphic(MessageChainContent::class) {
                subclass(MessageChainContent.AT::class)
                subclass(MessageChainContent.Text::class)
                subclass(MessageChainContent.Image::class)
            }
        }
    }

    fun makeMessage(msg: MsgRecord): Message {
        return Message(
            messageId = msg.msgId,
            userId = msg.senderUin,
            time = msg.msgTime,
            messageType = MessageType.GROUP,
            message = ArrayList<MessageChain>().apply {
                //添加各种类型的消息
                add(
                    MessageChain(
                        MessageChainType.TEXT,
                        MessageChainContent.Text(text = "114514")
                    ))
                add(
                    MessageChain(
                        MessageChainType.AT,
                        MessageChainContent.AT(qq = "114514")
                    ))
                add(
                    MessageChain(
                        MessageChainType.IMAGE,
                        MessageChainContent.Image(file = "?",url = "?", subType = "?")
                    ))
            },
            postType = "message"
        )
    }

    fun messageToJsonString(message: Message): String {
        return jsonEncoder.encodeToString(message)
    }

}