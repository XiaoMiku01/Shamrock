package com.tencent.qqnt.helper

internal abstract class InternalMessageMakerError(why: String): RuntimeException(why)

internal class ParamsException(key: String): InternalMessageMakerError("Lack of param $key")

internal class IllegalParamsException(key: String): InternalMessageMakerError("Illegal param $key")

internal class LogicException(why: String) : InternalMessageMakerError(why)

internal class ErrorTokenException : InternalMessageMakerError("access_token error")

