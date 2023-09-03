package moe.fuqiuluo.proto

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import kotlinx.serialization.json.JsonElement
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import moe.fuqiuluo.xposed.tools.jsonObject
import moe.fuqiuluo.xposed.tools.toHexString

sealed interface Proto {
    fun toJson(): JsonElement

    fun computeSize(tag: Int): Int

    fun writeTo(output: CodedOutputStream, tag: Int)

    fun computeSizeDirectly(): Int {
        return 0
    }

    operator fun contains(tag: Int): Boolean {
        if (this is ProtoMap) {
            return value.containsKey(tag)
        }
        error("Instance is not ProtoMap")
    }

    operator fun set(tag: Int, v: Proto) {
        if (this is ProtoMap) {
            if (!contains(tag)) {
                value[tag] = v
            } else {
                val oldValue = value[tag]!!
                if (oldValue is ProtoList) {
                    oldValue.add(v)
                } else {
                    value[tag] = ProtoList(arrayListOf(oldValue, v))
                }
            }
            return
        }
        error("Instance is not ProtoMap, it is ${this::class.simpleName}")
    }

    operator fun set(tag: Int, v: Number) {
        if (this is ProtoMap) {
            if (!contains(tag)) {
                value[tag] = ProtoNumber(v)
            } else {
                val oldValue = value[tag]!!
                if (oldValue is ProtoList) {
                    oldValue.add(v.proto)
                } else {
                    value[tag] = ProtoList(arrayListOf(oldValue, v.proto))
                }
            }
            return
        }
        error("Instance is not ProtoMap")
    }

    operator fun get(vararg tags: Int): Proto {
        if (this is ProtoMap) {
            var curMap = value
            tags.forEachIndexed { index, tag ->
                if (index == tags.size - 1) {
                    return curMap[tag] ?: error("Tag $tag not found")
                }
                curMap[tag]?.let { v ->
                    if (v is ProtoMap) {
                        curMap = v.value
                    } else {
                        return v
                    }
                } ?: error("Tag $tag not found")
            }
        }
        error("Instance is not ProtoMap")
    }

    fun add(v: Proto) {
        if (this is ProtoList) {
            value.add(v)
            return
        }
        error("Instance is not ProtoList")
    }
}

class ProtoNumber(
    val value: Number
): Proto {
    override fun toJson(): JsonElement {
        return value.json
    }

    override fun computeSize(tag: Int): Int {
        return when (value) {
            is Int -> CodedOutputStream.computeInt32Size(tag, value)
            is Long -> CodedOutputStream.computeInt64Size(tag, value)
            is Float -> CodedOutputStream.computeFloatSize(tag, value)
            is Double -> CodedOutputStream.computeDoubleSize(tag, value)
            else -> error("ProcCodec not support number type: ${value::class.simpleName}")
        }
    }

    override fun writeTo(output: CodedOutputStream, tag: Int) {
        when (value) {
            is Int -> output.writeInt32(tag, value)
            is Long -> output.writeInt64(tag, value)
            is Float -> output.writeFloat(tag, value)
            is Double -> output.writeDouble(tag, value)
            else -> error("ProcCodec not support number type: ${value::class.simpleName}")
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun toString(): String {
        return "Number($value)"
    }
}

class ProtoByteString(
    val value: ByteString
): Proto, Iterable<Byte> by value {
    override fun toJson(): JsonElement {
        return toByteArray().toHexString().json
    }

    override fun computeSize(tag: Int): Int {
        return CodedOutputStream.computeBytesSize(tag, value)
    }

    override fun writeTo(output: CodedOutputStream, tag: Int) {
        output.writeBytes(tag, value)
    }

    fun toByteArray(): ByteArray {
        return value.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    fun toUtfString(): String {
        return value.toStringUtf8()
    }

    override fun toString(): String {
        return "ByteString(${value.toByteArray().toHexString()})"
    }
}

class ProtoList(
    val value: ArrayList<Proto>
): Proto, Collection<Proto> by value {
    constructor(): this(arrayListOf())

    override fun toJson(): JsonElement {
        val array = arrayListOf<JsonElement>()
        forEach {
            array.add(it.toJson())
        }
        return array.jsonArray
    }

    override fun computeSize(tag: Int): Int {
        var size = 0
        value.forEach {
            size += it.computeSize(tag)
        }
        return size
    }

    override fun writeTo(output: CodedOutputStream, tag: Int) {
        value.forEach {
            it.writeTo(output, tag)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    override fun toString(): String {
        return toJson().toString()
    }
}

class ProtoMap(
    val value: HashMap<Int, Proto>
): Proto {
    constructor(): this(hashMapOf())

    operator fun set(vararg tags: Int, v: Proto) {
        var curProtoMap: ProtoMap = this
        tags.forEachIndexed { index, tag ->
            if (index == tags.size - 1) {
                return@forEachIndexed
            }
            if (!curProtoMap.contains(tag)) {
                val tmp = ProtoMap(hashMapOf())
                curProtoMap[tag] = tmp
                curProtoMap = tmp
            } else {
                curProtoMap = curProtoMap[tag].asMap
            }
        }
        curProtoMap[tags.last()] = v
    }

    operator fun set(vararg tags: Int, struct: (ProtoMap) -> Unit) {
        val map = ProtoMap()
        struct.invoke(map)
        set(*tags, v = map)
    }

    operator fun set(vararg tags: Int, v: String) {
        set(*tags, v = v.proto)
    }

    operator fun set(vararg tags: Int, v: ByteArray) {
        set(*tags, v = v.proto)
    }

    operator fun set(vararg tags: Int, v: Number) {
        set(*tags, v = v.proto)
    }

    operator fun set(vararg tags: Int, v: ByteString) {
        set(*tags, v = v.proto)
    }

    operator fun set(vararg tags: Int, v: Any) {
        set(*tags, v = any2proto(v))
    }

    override fun toJson(): JsonElement {
        val hashMap = hashMapOf<String, JsonElement>()
        value.forEach { (tag, field) ->
            hashMap[tag.toString()] = field.toJson()
        }
        return hashMap.jsonObject
    }

    override fun computeSize(tag: Int): Int {
        var size = CodedOutputStream.computeTagSize(tag)
        val dataSize = computeSizeDirectly()
        size += computeRawVarint32Size(dataSize)
        size += dataSize
        return size
    }

    override fun writeTo(output: CodedOutputStream, tag: Int) {
        output.writeTag(tag, WireFormat.WIRETYPE_LENGTH_DELIMITED)
        val dataSize = computeSizeDirectly()
        output.writeUInt32NoTag(dataSize)
        value.forEach { (tag, proto) ->
            proto.writeTo(output, tag)
        }
    }

    override fun computeSizeDirectly(): Int {
        var size = 0
        value.forEach { (tag, proto) ->
            size += proto.computeSize(tag)
        }
        return size
    }

    override fun toString(): String {
        return toJson().toString()
    }

    fun toByteArray(): ByteArray {
        return ProtoUtils.encodeToByteArray(this)
    }

    companion object {
        private fun computeRawVarint32Size(size: Int): Int {
            if (size and -128 == 0) {
                return 1
            }
            if (size and -16384 == 0) {
                return 2
            }
            if (-2097152 and size == 0) {
                return 3
            }
            return if (size and -268435456 == 0) 4 else 5
        }

        private fun any2proto(any: Any): Proto {
            return when(any) {
                is Number -> any.proto
                is ByteArray -> any.proto
                is String -> any.proto
                is ByteString -> any.proto
                is Array<*> -> ProtoList(arrayListOf(*any.map { any2proto(it!!) }.toTypedArray()))
                is Collection<*> -> ProtoList(arrayListOf(*any.map { any2proto(it!!) }.toTypedArray()))
                is Map<*, *> -> ProtoMap(hashMapOf(*any.map { (k, v) ->
                    k as Int to any2proto(v!!)
                }.toTypedArray()))
                else -> error("Not support type: ${any::class.simpleName}")
            }
        }
    }
}

val Number.proto: ProtoNumber
    get() = ProtoNumber(this)

val ByteString.proto: ProtoByteString
    get() = ProtoByteString(this)

val ByteArray.proto: ProtoByteString
    get() = ProtoByteString(ByteString.copyFrom(this))

val String.proto: ProtoByteString
    get() = ProtoByteString(ByteString.copyFromUtf8(this))

val Proto.asString: ByteString
    get() = (this as ProtoByteString).value

val Proto.asNumber: Number
    get() = (this as ProtoNumber).value

val Proto.asInt: Int
    get() = (this as ProtoNumber).value.toInt()

val Proto.asLong: Long
    get() = (this as ProtoNumber).value.toLong()

val Proto.asMap: ProtoMap
    get() = (this as ProtoMap)

val Proto.asList: ProtoList
    get() = (this as ProtoList)

val Proto.asByteArray: ByteArray
    get() = (this as ProtoByteString).toByteArray()

val Proto.asUtf8String: String
    get() = (this as ProtoByteString).toUtfString()

fun protobufOf(struct: (ProtoMap) -> Unit): ProtoMap {
    val map = ProtoMap()
    struct.invoke(map)
    return map
}
