package moe.fuqiuluo.proto

import com.google.protobuf.CodedOutputStream
import com.google.protobuf.UnknownFieldSet

object ProtoUtils {

    fun decodeFromByteArray(data: ByteArray): ProtoMap {
        val unknownFieldSet = UnknownFieldSet.parseFrom(data)
        val dest = ProtoMap(hashMapOf())
        printUnknownFieldSet(unknownFieldSet, dest)
        return dest
    }

    fun encodeToByteArray(protoMap: ProtoMap): ByteArray {
        val size = protoMap.computeSizeDirectly()
        val dest = ByteArray(size)
        val output = CodedOutputStream.newInstance(dest)
        protoMap.value.forEach { (tag, proto) ->
            proto.writeTo(output, tag)
        }
        output.checkNoSpaceLeft()
        return dest
    }


    private fun printUnknownFieldSet(set: UnknownFieldSet, dest: ProtoMap) {
        set.asMap().forEach { (tag, field) ->
            field.varintList.forEach {
                dest[tag] = it
            }
            field.fixed32List.forEach {
                dest[tag] = it
            }
            field.fixed64List.forEach {
                dest[tag] = it
            }
            field.lengthDelimitedList.forEach {
                try {
                    val unknownFieldSet = UnknownFieldSet.parseFrom(it)
                    val map = ProtoMap(hashMapOf())
                    printUnknownFieldSet(unknownFieldSet, map)
                    dest[tag] = map
                } catch (e: Throwable) {
                    dest[tag] = it.proto
                }
            }
            field.groupList.forEach {
                val map = ProtoMap(hashMapOf())
                printUnknownFieldSet(it, map)
                dest[tag] = map
            }
        }
    }
}

