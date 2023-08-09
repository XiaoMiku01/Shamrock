package com.tencent.mobileqq.listener.data.sub

open class MessageChainContent{
    class AT(val qq:String): MessageChainContent()
    class Text(val text:String): MessageChainContent()
    class Image(val file:String,val url:String,val subType:String): MessageChainContent()
}