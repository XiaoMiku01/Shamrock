package com.tencent.qqnt.protocol

import com.tencent.mobileqq.transfile.FileMsg
import com.tencent.mobileqq.transfile.api.IProtoReqManager
import com.tencent.mobileqq.transfile.protohandler.RichProto
import com.tencent.mobileqq.transfile.protohandler.RichProtoProc
import com.tencent.qqnt.transfile.FileTransfer
import com.tencent.qqnt.utils.PlatformUtils
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import mqq.app.MobileQQ
import kotlin.coroutines.resume

internal object RichProtoSvc: BaseSvc() {
    suspend fun getC2CVideoDownUrl(
        peerId: String,
        md5Hex: String,
        fileUUId: String
    ): String {
        return suspendCancellableCoroutine {
            val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
            val richProtoReq = RichProto.RichProtoReq()
            val downReq: RichProto.RichProtoReq.ShortVideoDownReq = RichProto.RichProtoReq.ShortVideoDownReq()
            downReq.selfUin = runtime.currentAccountUin
            downReq.peerUin = peerId
            downReq.secondUin = peerId
            downReq.uinType = FileMsg.UIN_BUDDY
            downReq.agentType = 0
            downReq.chatType = 1
            downReq.troopUin = peerId
            downReq.clientType = 2
            downReq.fileId = fileUUId
            downReq.md5 = md5Hex.hex2ByteArray()
            downReq.busiType = FileTransfer.BUSI_TYPE_SHORT_VIDEO
            downReq.subBusiType = 0
            downReq.fileType = FileTransfer.VIDEO_FORMAT_MP4
            downReq.downType = 1
            downReq.sceneType = 1
            richProtoReq.callback = RichProtoProc.RichProtoCallback { _, resp ->
                if (resp.resps.isEmpty() || resp.resps.first().errCode != 0) {
                    LogCenter.log("requestDownPrivateVideo: ${resp.resps.firstOrNull()?.errCode}", Level.WARN)
                    it.resume("")
                } else {
                    val videoDownResp = resp.resps.first() as RichProto.RichProtoResp.ShortVideoDownResp
                    val url = StringBuilder()
                    url.append(videoDownResp.mIpList.random().getServerUrl("http://"))
                    url.append(videoDownResp.mUrl)
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.SHORT_VIDEO_DW
            richProtoReq.reqs.add(downReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

    suspend fun getGroupVideoDownUrl(
        peerId: String,
        md5Hex: String,
        fileUUId: String
    ): String {
        return suspendCancellableCoroutine {
            val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
            val richProtoReq = RichProto.RichProtoReq()
            val downReq: RichProto.RichProtoReq.ShortVideoDownReq = RichProto.RichProtoReq.ShortVideoDownReq()
            downReq.selfUin = runtime.currentAccountUin
            downReq.peerUin = peerId
            downReq.secondUin = peerId
            downReq.uinType = FileMsg.UIN_TROOP
            downReq.agentType = 0
            downReq.chatType = 1
            downReq.troopUin = peerId
            downReq.clientType = 2
            downReq.fileId = fileUUId
            downReq.md5 = md5Hex.hex2ByteArray()
            downReq.busiType = FileTransfer.BUSI_TYPE_SHORT_VIDEO
            downReq.subBusiType = 0
            downReq.fileType = FileTransfer.VIDEO_FORMAT_MP4
            downReq.downType = 1
            downReq.sceneType = 1
            richProtoReq.callback = RichProtoProc.RichProtoCallback { _, resp ->
                if (resp.resps.isEmpty() || resp.resps.first().errCode != 0) {
                    LogCenter.log("requestDownGroupVideo: ${resp.resps.firstOrNull()?.errCode}", Level.WARN)
                    it.resume("")
                } else {
                    val videoDownResp = resp.resps.first() as RichProto.RichProtoResp.ShortVideoDownResp
                    val url = StringBuilder()
                    url.append(videoDownResp.mIpList.random().getServerUrl("http://"))
                    url.append(videoDownResp.mUrl)
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.SHORT_VIDEO_DW
            richProtoReq.reqs.add(downReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

    suspend fun getC2CPttDownUrl(
        peerId: String,
        fileUUId: String
    ): String {
        return suspendCancellableCoroutine {
            val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
            val richProtoReq = RichProto.RichProtoReq()
            val pttDownReq: RichProto.RichProtoReq.C2CPttDownReq = RichProto.RichProtoReq.C2CPttDownReq()
            pttDownReq.selfUin = runtime.currentAccountUin
            pttDownReq.peerUin = peerId
            pttDownReq.secondUin = peerId
            pttDownReq.uinType = FileMsg.UIN_BUDDY
            pttDownReq.busiType = 1002
            pttDownReq.uuid = fileUUId
            pttDownReq.storageSource = "pttcenter"
            pttDownReq.isSelfSend = false

            pttDownReq.voiceType = 1
            pttDownReq.downType = 1
            richProtoReq.callback = RichProtoProc.RichProtoCallback { _, resp ->
                if (resp.resps.isEmpty() || resp.resps.first().errCode != 0) {
                    LogCenter.log("requestDownPrivateVoice: ${resp.resps.firstOrNull()?.errCode}", Level.WARN)
                    it.resume("")
                } else {
                    val pttDownResp = resp.resps.first() as RichProto.RichProtoResp.C2CPttDownResp
                    val url = StringBuilder()
                    url.append(pttDownResp.downloadUrl)
                    url.append("&client_proto=qq&client_appid=${MobileQQ.getMobileQQ().appId}&client_type=android&client_ver=${PlatformUtils.getVersion(MobileQQ.getContext())}&client_down_type=auto&client_aio_type=unk")
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.C2C_PTT_DW
            richProtoReq.reqs.add(pttDownReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

    suspend fun getGroupPttDownUrl(
        peerId: String,
        md5Hex: String,
        fileUUId: String
    ): String {
        return suspendCancellableCoroutine {
            val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
            val richProtoReq = RichProto.RichProtoReq()
            val groupPttDownReq: RichProto.RichProtoReq.GroupPttDownReq = RichProto.RichProtoReq.GroupPttDownReq()
            groupPttDownReq.selfUin = runtime.currentAccountUin
            groupPttDownReq.peerUin = peerId
            groupPttDownReq.secondUin = peerId
            groupPttDownReq.uinType = FileMsg.UIN_TROOP
            groupPttDownReq.groupFileID = 0
            groupPttDownReq.groupFileKey = fileUUId
            groupPttDownReq.md5 = md5Hex.hex2ByteArray()
            groupPttDownReq.voiceType = 1
            groupPttDownReq.downType = 1
            richProtoReq.callback = RichProtoProc.RichProtoCallback { _, resp ->
                if (resp.resps.isEmpty() || resp.resps.first().errCode != 0) {
                    LogCenter.log("requestDownGroupVoice: ${resp.resps.firstOrNull()?.errCode}", Level.WARN)
                    it.resume("")
                } else {
                    val pttDownResp = resp.resps.first() as RichProto.RichProtoResp.GroupPttDownResp
                    val url = StringBuilder()
                    url.append("http://")
                    url.append(pttDownResp.domainV4V6)
                    url.append(pttDownResp.urlPath)
                    url.append("&client_proto=qq&client_appid=${MobileQQ.getMobileQQ().appId}&client_type=android&client_ver=${
                        PlatformUtils.getVersion(
                            MobileQQ.getContext())}&client_down_type=auto&client_aio_type=unk")
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.GRP_PTT_DW
            richProtoReq.reqs.add(groupPttDownReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

}