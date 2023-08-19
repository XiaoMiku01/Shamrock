@file:OptIn(DelicateCoroutinesApi::class)

package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.data.MessageForShortVideo
import com.tencent.mobileqq.transfile.FileMsg
import com.tencent.mobileqq.transfile.TransferRequest
import com.tencent.mobileqq.transfile.api.IProtoReqManager
import com.tencent.mobileqq.transfile.api.ITransFileController
import com.tencent.mobileqq.transfile.protohandler.RichProto
import com.tencent.mobileqq.transfile.protohandler.RichProtoProc
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import moe.fuqiuluo.xposed.helper.PlatformHelper
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import mqq.app.AppRuntime
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tools.MD5
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.random.Random

internal object HighwayHelper {
    const val SEND_MSG_BUSINESS_TYPE_AIO_ALBUM_PIC = 1031
    const val SEND_MSG_BUSINESS_TYPE_AIO_KEY_WORD_PIC = 1046
    const val SEND_MSG_BUSINESS_TYPE_AIO_QZONE_PIC = 1045
    const val SEND_MSG_BUSINESS_TYPE_ALBUM_PIC = 1007
    const val SEND_MSG_BUSINESS_TYPE_BLESS = 1056
    const val SEND_MSG_BUSINESS_TYPE_CAPTURE_PIC = 1008
    const val SEND_MSG_BUSINESS_TYPE_COMMEN_FALSH_PIC = 1040
    const val SEND_MSG_BUSINESS_TYPE_CUSTOM = 1006
    const val SEND_MSG_BUSINESS_TYPE_DOUTU_PIC = 1044
    const val SEND_MSG_BUSINESS_TYPE_FALSH_PIC = 1039
    const val SEND_MSG_BUSINESS_TYPE_FAST_IMAGE = 1037
    const val SEND_MSG_BUSINESS_TYPE_FORWARD_EDIT = 1048
    const val SEND_MSG_BUSINESS_TYPE_FORWARD_PIC = 1009
    const val SEND_MSG_BUSINESS_TYPE_FULL_SCREEN_ESSENCE = 1057
    const val SEND_MSG_BUSINESS_TYPE_GALEERY_PIC = 1041
    const val SEND_MSG_BUSINESS_TYPE_GAME_CENTER_STRATEGY = 1058
    const val SEND_MSG_BUSINESS_TYPE_HOT_PIC = 1042
    const val SEND_MSG_BUSINESS_TYPE_MIXED_PICS = 1043
    const val SEND_MSG_BUSINESS_TYPE_PIC_AIO_ALBUM = 1052
    const val SEND_MSG_BUSINESS_TYPE_PIC_CAMERA = 1050
    const val SEND_MSG_BUSINESS_TYPE_PIC_FAV = 1053
    const val SEND_MSG_BUSINESS_TYPE_PIC_SCREEN = 1027
    const val SEND_MSG_BUSINESS_TYPE_PIC_SHARE = 1030
    const val SEND_MSG_BUSINESS_TYPE_PIC_TAB_CAMERA = 1051
    const val SEND_MSG_BUSINESS_TYPE_QQPINYIN_SEND_PIC = 1038
    const val SEND_MSG_BUSINESS_TYPE_RECOMMENDED_STICKER = 1047
    const val SEND_MSG_BUSINESS_TYPE_RELATED_EMOTION = 1054
    const val SEND_MSG_BUSINESS_TYPE_SHOWLOVE = 1036
    const val SEND_MSG_BUSINESS_TYPE_SOGOU_SEND_PIC = 1034
    const val SEND_MSG_BUSINESS_TYPE_TROOP_BAR = 1035
    const val SEND_MSG_BUSINESS_TYPE_WLAN_RECV_NOTIFY = 1055
    const val SEND_MSG_BUSINESS_TYPE_ZHITU_PIC = 1049
    const val SEND_MSG_BUSINESS_TYPE_ZPLAN_EMOTICON_GIF = 1060
    const val SEND_MSG_BUSINESS_TYPE_ZPLAN_PIC = 1059

    const val VIDEO_FORMAT_AFS = 7
    const val VIDEO_FORMAT_AVI = 1
    const val VIDEO_FORMAT_MKV = 4
    const val VIDEO_FORMAT_MOD = 9
    const val VIDEO_FORMAT_MOV = 8
    const val VIDEO_FORMAT_MP4 = 2
    const val VIDEO_FORMAT_MTS = 11
    const val VIDEO_FORMAT_RM = 6
    const val VIDEO_FORMAT_RMVB = 5
    const val VIDEO_FORMAT_TS = 10
    const val VIDEO_FORMAT_WMV = 3

    const val BUSI_TYPE_GUILD_VIDEO = 4601
    const val BUSI_TYPE_MULTI_FORWARD_VIDEO = 1010
    const val BUSI_TYPE_PUBACCOUNT_PERM_VIDEO = 1009
    const val BUSI_TYPE_PUBACCOUNT_TEMP_VIDEO = 1007
    const val BUSI_TYPE_SHORT_VIDEO = 1
    const val BUSI_TYPE_SHORT_VIDEO_PTV = 2
    const val BUSI_TYPE_VIDEO = 0
    const val BUSI_TYPE_VIDEO_EMOTICON_PIC = 1022
    const val BUSI_TYPE_VIDEO_EMOTICON_VIDEO = 1021

    suspend fun requestDownC2CVideo(
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
            downReq.busiType = BUSI_TYPE_SHORT_VIDEO
            downReq.subBusiType = 0
            downReq.fileType = VIDEO_FORMAT_MP4
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

    suspend fun requestDownGroupVideo(
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
            downReq.busiType = BUSI_TYPE_SHORT_VIDEO
            downReq.subBusiType = 0
            downReq.fileType = VIDEO_FORMAT_MP4
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

    suspend fun requestDownC2CPtt(
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
                    url.append("&client_proto=qq&client_appid=${MobileQQ.getMobileQQ().appId}&client_type=android&client_ver=${PlatformHelper.getVersion(MobileQQ.getContext())}&client_down_type=auto&client_aio_type=unk")
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.C2C_PTT_DW
            richProtoReq.reqs.add(pttDownReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

    suspend fun requestDownGroupPtt(
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
                    url.append("&client_proto=qq&client_appid=${MobileQQ.getMobileQQ().appId}&client_type=android&client_ver=${PlatformHelper.getVersion(MobileQQ.getContext())}&client_down_type=auto&client_aio_type=unk")
                    it.resume(url.toString())
                }
            }
            richProtoReq.protoKey = RichProtoProc.GRP_PTT_DW
            richProtoReq.reqs.add(groupPttDownReq)
            richProtoReq.protoReqMgr = runtime.getRuntimeService(IProtoReqManager::class.java, "all")
            RichProtoProc.procRichProtoReq(richProtoReq)
        }
    }

    suspend fun transC2CVideo(
        friendId: String,
        file: File,
        thumbFile: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.mSelfUin = runtime.currentAccountUin
        transferRequest.mPeerUin = friendId
        transferRequest.mUinType = FileMsg.UIN_BUDDY
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_SHORT_VIDEO_C2C
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mBusiType = BUSI_TYPE_SHORT_VIDEO
        transferRequest.mMd5 = MD5.getFileMD5(file)
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mSourceVideoCodecFormat = VIDEO_FORMAT_MP4
        transferRequest.mRec = MessageForShortVideo().also {
            it.busiType = BUSI_TYPE_SHORT_VIDEO
        }
        transferRequest.mThumbPath = thumbFile.absolutePath
        transferRequest.mThumbMd5 = MD5.getFileMD5(thumbFile)
        return transAndWait(runtime, transferRequest, wait)
    }

    suspend fun transTroopVideo(
        groupId: String,
        file: File,
        thumbFile: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.mSelfUin = runtime.currentAccountUin
        transferRequest.mPeerUin = groupId
        transferRequest.mUinType = FileMsg.UIN_TROOP
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_SHORT_VIDEO_TROOP
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mBusiType = BUSI_TYPE_SHORT_VIDEO
        transferRequest.mMd5 = MD5.getFileMD5(file)
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mSourceVideoCodecFormat = VIDEO_FORMAT_MP4
        transferRequest.mRec = MessageForShortVideo().also {
            it.busiType = BUSI_TYPE_SHORT_VIDEO
        }
        transferRequest.mThumbPath = thumbFile.absolutePath
        transferRequest.mThumbMd5 = MD5.getFileMD5(thumbFile)
        return transAndWait(runtime, transferRequest, wait)
    }

    suspend fun transC2CVoice(
        friendId: String,
        file: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.mSelfUin = runtime.currentAccountUin
        transferRequest.mPeerUin = friendId
        transferRequest.mUinType = FileMsg.UIN_BUDDY
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_PTT
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mBusiType = 1002
        transferRequest.mPttCompressFinish = true
        transferRequest.mPttUploadPanel = 3
        transferRequest.mIsPttPreSend = true
        return transAndWait(runtime, transferRequest, wait)
    }

    suspend fun transTroopVoice(
        groupId: String,
        file: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.mSelfUin = runtime.currentAccountUin
        transferRequest.mPeerUin = groupId
        transferRequest.mUinType = FileMsg.UIN_TROOP
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_PTT
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mLocalPath = file.absolutePath
        transferRequest.mBusiType = 1002
        transferRequest.mPttCompressFinish = true
        transferRequest.mPttUploadPanel = 3
        transferRequest.mIsPttPreSend = true
        return transAndWait(runtime, transferRequest, wait)
    }

    suspend fun transC2CPic(
        friendId: String,
        file: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.needSendMsg = false
        transferRequest.mSelfUin = runtime.account
        transferRequest.mPeerUin = friendId
        transferRequest.mSecondId = runtime.currentAccountUin
        transferRequest.mUinType = FileMsg.UIN_BUDDY
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_PIC
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mBusiType = SEND_MSG_BUSINESS_TYPE_PIC_SHARE
        transferRequest.mMd5 = MD5.getFileMD5(file)
        transferRequest.mLocalPath = file.absolutePath
        val picUpExtraInfo = TransferRequest.PicUpExtraInfo()
        picUpExtraInfo.mIsRaw = true
        transferRequest.mPicSendSource = 8
        transferRequest.mExtraObj = picUpExtraInfo
        return transAndWait(runtime, transferRequest, wait)
    }

    suspend fun transTroopPic(
        groupId: String,
        file: File,
        wait: Boolean = true
    ): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val transferRequest = TransferRequest()
        transferRequest.needSendMsg = false
        transferRequest.mSelfUin = runtime.account
        transferRequest.mPeerUin = groupId
        transferRequest.mSecondId = runtime.currentAccountUin
        transferRequest.mUinType = FileMsg.UIN_TROOP
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_PIC
        transferRequest.mUniseq = createMessageUniseq()
        transferRequest.mIsUp = true
        transferRequest.mBusiType = SEND_MSG_BUSINESS_TYPE_PIC_SHARE
        transferRequest.mMd5 = MD5.getFileMD5(file)
        transferRequest.mLocalPath = file.absolutePath
        val picUpExtraInfo = TransferRequest.PicUpExtraInfo()
        picUpExtraInfo.mIsRaw = true
        transferRequest.mPicSendSource = 8
        transferRequest.mExtraObj = picUpExtraInfo
        return transAndWait(runtime, transferRequest, wait)
    }

    private suspend fun transAndWait(
        runtime: AppRuntime,
        transferRequest: TransferRequest,
        wait: Boolean
    ): Boolean {
        val service = runtime.getRuntimeService(ITransFileController::class.java, "all")
        if(service.transferAsync(transferRequest)) {
            if (wait) {
                return true
            }
            return suspendCancellableCoroutine { continuation ->
                val waiter = GlobalScope.launch {
                    while (
                        service.findProcessor(transferRequest.keyForTransfer) != null
                    ) {
                        delay(1000)
                    }
                    continuation.resume(true)
                }
                continuation.invokeOnCancellation {
                    waiter.cancel()
                    continuation.resume(false)
                }
            }
        }
        return false
    }

    private fun createMessageUniseq(time: Long = System.currentTimeMillis()): Long {
        var uniseq = (time / 1000).toInt().toLong()
        uniseq = uniseq shl 32 or abs(Random.nextInt()).toLong()
        return uniseq
    }

}