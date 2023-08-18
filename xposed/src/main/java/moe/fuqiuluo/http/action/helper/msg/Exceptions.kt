package moe.fuqiuluo.http.action.helper.msg

internal interface InternalMessageMakerError

internal class ParamsException(key: String)
    :RuntimeException("Lack of param $key"), InternalMessageMakerError

internal class ParamsIllegalException(key: String)
    :RuntimeException("Illegal param $key"), InternalMessageMakerError

internal class LogicException(why: String)
    :RuntimeException(why), InternalMessageMakerError
