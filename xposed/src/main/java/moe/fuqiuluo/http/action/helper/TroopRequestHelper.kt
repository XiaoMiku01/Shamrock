package moe.fuqiuluo.http.action.helper

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.app.BusinessHandlerFactory
import mqq.app.MobileQQ
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object TroopRequestHelper {
    private lateinit var METHOD_REQ_MEMBER_INFO: Method
    private lateinit var METHOD_REQ_MEMBER_INFO_V2: Method
    private lateinit var METHOD_REQ_TROOP_LIST: Method
    private lateinit var METHOD_REQ_TROOP_MEM_LIST: Method

    fun refreshTroopMemberList(groupId: Long) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_MEMBER_LIST_HANDLER)

        // void C(boolean foreRefresh, String groupId, String troopcode, int reqType); // RequestedTroopList/refreshMemberListFromServer
        if (!::METHOD_REQ_TROOP_MEM_LIST.isInitialized) {
            METHOD_REQ_TROOP_MEM_LIST = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 4
                        && it.parameterTypes[0] == Boolean::class.java
                        && it.parameterTypes[1] == String::class.java
                        && it.parameterTypes[2] == String::class.java
                        && it.parameterTypes[3] == Int::class.java
                        && !Modifier.isPrivate(it.modifiers)
            }
        }

        METHOD_REQ_TROOP_MEM_LIST.invoke(businessHandler, true, groupId.toString(), groupUin2GroupCode(groupId).toString(), 5)
    }

    fun refreshTroopList() {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_LIST_HANDLER)

        if (!::METHOD_REQ_TROOP_LIST.isInitialized) {
            METHOD_REQ_TROOP_LIST = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 0 && !Modifier.isPrivate(it.modifiers) && it.returnType == Void.TYPE
            }
        }

        METHOD_REQ_TROOP_LIST.invoke(businessHandler)
    }

    fun requestMemberInfo(groupId: Long, memberUin: Long) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_MEMBER_CARD_HANDLER)

        if (!::METHOD_REQ_MEMBER_INFO.isInitialized) {
            METHOD_REQ_MEMBER_INFO = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 2 &&
                        it.parameterTypes[0] == Long::class.java &&
                        it.parameterTypes[1] == Long::class.java &&
                        !Modifier.isPrivate(it.modifiers)
            }
        }

        METHOD_REQ_MEMBER_INFO.invoke(businessHandler, groupId, memberUin)
    }

    fun requestMemberInfoV2(groupId: Long, memberUin: Long) {
        val app = MobileQQ.getMobileQQ().waitAppRuntime()
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_MEMBER_CARD_HANDLER)

        if (!::METHOD_REQ_MEMBER_INFO_V2.isInitialized) {
            METHOD_REQ_MEMBER_INFO_V2 = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 3 &&
                        it.parameterTypes[0] == String::class.java &&
                        it.parameterTypes[1] == String::class.java &&
                        !Modifier.isPrivate(it.modifiers)
            }
        }

        METHOD_REQ_MEMBER_INFO_V2.invoke(businessHandler, groupId.toString(), groupUin2GroupCode(groupId).toString(), arrayListOf(memberUin.toString()))
    }

    fun groupUin2GroupCode(groupuin: Long): Long {
        var calc = groupuin / 1000000L
        while (true) {
            calc -= if (calc >= 0 + 202 && calc + 202 <= 10) {
                (202 - 0).toLong()
            } else if (calc >= 11 + 480 && calc <= 19 + 480) {
                (480 - 11).toLong()
            } else if (calc >= 20 + 2100 && calc <= 66 + 2100) {
                (2100 - 20).toLong()
            } else if (calc >= 67 + 2010 && calc <= 156 + 2010) {
                (2010 - 67).toLong()
            } else if (calc >= 157 + 2147 && calc <= 209 + 2147) {
                (2147 - 157).toLong()
            } else if (calc >= 210 + 4100 && calc <= 309 + 4100) {
                (4100 - 210).toLong()
            } else if (calc >= 310 + 3800 && calc <= 499 + 3800) {
                (3800 - 310).toLong()
            } else {
                break
            }
        }
        return calc * 1000000L + groupuin % 1000000L
    }
}