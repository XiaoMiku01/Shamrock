package com.tencent.mobileqq.listener.data.sub

import com.tencent.mobileqq.listener.data.Response

class Message(val message_id:Long,
              val user_id:Long,
              val time:Long,
              val message_type:MessageType,
              val message:ArrayList<MessageChain>
    ) : Response(post_type = "message") {
    enum class MessageType{
        PRIVATE,GROUP;
        override fun toString():String{
            return when(this){
                PRIVATE -> "private"
                GROUP -> "group"
            }
        }
    }
    class MessageChain(val type:MessageChainType,val data: MessageChainContent){
        enum class MessageChainType{
            AT,TEXT,IMAGE;
            override fun toString():String{
                return when(this){
                    AT -> "at"
                    TEXT -> "text"
                    IMAGE -> "image"
                }
            }
        }
    }
}