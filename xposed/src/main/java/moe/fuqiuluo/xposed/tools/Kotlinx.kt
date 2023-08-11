package moe.fuqiuluo.xposed.tools

import java.util.Locale

class Nullable<T: Any>(
    private var value: T?
) {
    fun get(): T {
        return value!!
    }

    fun getOrNull(): T? {
        return value
    }

    fun isNull(): Boolean {
        return value == null
    }

    fun isNotNull(): Boolean {
        return value != null
    }

    fun set(value: T?) {
        this.value = value
    }
}

fun <T: Any> nullableOf(data: T? = null): Nullable<T> {
    return Nullable(data)
}

fun ByteArray.slice(off: Int, length: Int = size - off): ByteArray {
    if (isNotEmpty()) {
        val b1 = ByteArray(length)
        System.arraycopy(this, off, b1, 0, length)
        return b1
    }
    return this
}

@JvmOverloads fun ByteArray.toHexString(uppercase: Boolean = false): String = this.joinToString("") {
    (it.toInt() and 0xFF).toString(16)
        .padStart(2, '0')
        .let { s -> if (uppercase) s.lowercase(Locale.getDefault()) else s }
}

fun String?.ifNullOrEmpty(defaultValue: String?): String? {
    return if (this.isNullOrEmpty()) defaultValue else this
}