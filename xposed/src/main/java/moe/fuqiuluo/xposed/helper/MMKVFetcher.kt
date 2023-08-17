package moe.fuqiuluo.xposed.helper

import com.tencent.mmkv.MMKV
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal object MMKVFetcher {
    private lateinit var METHOD_GET_MMKV: Method

    fun defaultMMKV(): MMKV {
        if (!::METHOD_GET_MMKV.isInitialized) {
            METHOD_GET_MMKV = MMKV::class.java.declaredMethods.first {
                Modifier.isStatic(it.modifiers) && it.parameterCount == 0 && it.returnType == MMKV::class.java
            }
        }
        return METHOD_GET_MMKV.invoke(null) as MMKV
    }
}