package moe.fuqiuluo.http.action.helper

import com.tencent.proto.lbsshare.LBSShare
import moe.fuqiuluo.http.action.helper.msg.ParamsIllegalException

internal object LocationHelper {
    init {

    }

    fun getLocationWithLonLat(lat: Double, lon: Double) {
        if (lat > 90 || lat < 0) {
            throw ParamsIllegalException("纬度大小错误")
        }
        if (lon > 180 || lon < 0) {
            throw ParamsIllegalException("经度大小错误")
        }

        val req = LBSShare.LocationReq()

    }

}