package com.tencent.mobileqq.service

import SummaryCard.RespHead
import SummaryCard.RespSummaryCard
import android.os.Bundle
import android.util.SparseArray
import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.profilecard.entity.BusinessReqBuffer
import com.tencent.mobileqq.profilecard.entity.BusinessRespBuffer
import com.tencent.mobileqq.profilecard.processor.AbsProfileBusinessProcessor
import mqq.app.AppRuntime
import tencent.im.oidb.cmd0x5eb.oidb_0x5eb.UdcUinData
import java.nio.ByteBuffer

internal class ProfileProcessor(appRuntime: AppRuntime?) : AbsProfileBusinessProcessor(appRuntime) {
    override fun onGetProfileDetailRequestForLogin(list: List<Short>) {

    }

    override fun onGetProfileDetailResponseBegin(bundle: Bundle) {}

    override fun onGetProfileDetailResponseEnd(bundle: Bundle, z: Boolean, card: Card) {

    }

    override fun onGetProfileDetailTLV(
        bundle: Bundle,
        j2: Long,
        card: Card,
        s: Short,
        s2: Short,
        byteBuffer: ByteBuffer?
    ) {
    }

    override fun onProcessProfile0x5eb(
        bundle: Bundle?,
        card: Card?,
        respHead: RespHead?,
        respSummaryCard: RespSummaryCard?,
        oidb_0x5eb_udcuindata: UdcUinData?
    ) {
    }

    override fun onGetProfileDetailTLVBegin(bundle: Bundle?, j2: Long, card: Card?) {}

    override fun onGetProfileDetailTLVEnd(bundle: Bundle?, j2: Long, card: Card?) {}

    override fun onProcessProfileCard(
        bundle: Bundle?,
        card: Card?,
        respHead: RespHead?,
        respSummaryCard: RespSummaryCard?
    ) {
    }

    override fun onProcessProfileService(
        bundle: Bundle?,
        card: Card?,
        respHead: RespHead?,
        respSummaryCard: RespSummaryCard?,
        sparseArray: SparseArray<BusinessRespBuffer?>?
    ) {
    }

    override fun onRequestProfileCard(
        bundle: Bundle?,
        arrayList: ArrayList<BusinessReqBuffer?>?,
        arrayList2: ArrayList<Int?>?
    ) {
    }

    override fun onResponseProfileCard(
        z: Boolean,
        bundle: Bundle?,
        respHead: RespHead?,
        respSummaryCard: RespSummaryCard?
    ) {
    }

    override fun requestParseProfileLocation(card: Card?) {}
}