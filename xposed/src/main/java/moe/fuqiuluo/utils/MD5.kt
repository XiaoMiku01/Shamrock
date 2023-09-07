package moe.fuqiuluo.utils

object MD5 {
    external fun getMd5Hex(bytes: ByteArray): String

    external fun genFileMd5Hex(filePath: String): String
}