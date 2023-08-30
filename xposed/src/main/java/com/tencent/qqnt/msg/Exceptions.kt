package com.tencent.qqnt.msg

internal interface InternalMessageMakerError

internal class ParamsException(key: String)
    :RuntimeException("Lack of param $key"), InternalMessageMakerError

internal class ParamsIllegalException(key: String)
    :RuntimeException("Illegal param $key"), InternalMessageMakerError

internal class LogicException(why: String)
    :RuntimeException(why), InternalMessageMakerError

internal class ErrorTokenException()
    :RuntimeException("access_token error"), InternalMessageMakerError

