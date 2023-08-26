package moe.fuqiuluo.xposed.ipc

import android.os.Parcel
import android.os.Parcelable

data class IQSign(
    val token: ByteArray,
    val extra: ByteArray,
    val sign: ByteArray
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createByteArray()!!,
        parcel.createByteArray()!!,
        parcel.createByteArray()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(token)
        parcel.writeByteArray(extra)
        parcel.writeByteArray(sign)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IQSign> {
        override fun createFromParcel(parcel: Parcel): IQSign {
            return IQSign(parcel)
        }

        override fun newArray(size: Int): Array<IQSign?> {
            return arrayOfNulls(size)
        }
    }
}