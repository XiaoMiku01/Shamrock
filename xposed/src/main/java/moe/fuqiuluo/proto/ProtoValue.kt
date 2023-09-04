package moe.fuqiuluo.proto

import com.google.protobuf.CodedOutputStream
import kotlinx.serialization.json.JsonElement

sealed interface ProtoValue {
    fun toJson(): JsonElement

    fun computeSize(tag: Int): Int

    fun writeTo(output: CodedOutputStream, tag: Int)

    fun computeSizeDirectly(): Int {
        return 0
    }

    operator fun contains(tag: Int): Boolean {
        return false
    }

    operator fun set(tag: Int, v: ProtoValue) {
        return
    }

    operator fun set(tag: Int, v: Number) {
        return
    }

    operator fun get(vararg tags: Int): ProtoValue {
        error("Instance is not ProtoMap")
    }

    fun add(v: ProtoValue) {
        error("Instance is not ProtoList")
    }

    fun size(): Int {
        return 0
    }
}
