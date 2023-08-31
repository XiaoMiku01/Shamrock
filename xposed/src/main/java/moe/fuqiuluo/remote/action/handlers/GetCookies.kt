package moe.fuqiuluo.remote.action.handlers

import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.pb.ByteStringMicro
import com.tencent.mobileqq.transfile.FileMsg
import com.tencent.mobileqq.transfile.TransferRequest
import com.tencent.mobileqq.transfile.api.IProtoReqManager
import com.tencent.mobileqq.transfile.api.ITransFileController
import com.tencent.mobileqq.transfile.protohandler.RichProto
import com.tencent.mobileqq.transfile.protohandler.RichProto.RichProtoReq
import com.tencent.mobileqq.transfile.protohandler.RichProto.RichProtoReq.MultiMsgUpReq
import com.tencent.mobileqq.transfile.protohandler.RichProtoProc
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.protocol.MsgSvc
import com.tencent.qqnt.transfile.FileTransfer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import moe.fuqiuluo.utils.DeflateTools
import moe.fuqiuluo.xposed.tools.hex2ByteArray
import moe.fuqiuluo.xposed.tools.json
import moe.fuqiuluo.xposed.tools.jsonArray
import mqq.app.MobileQQ
import msf.msgcomm.msg_comm
import msf.msgsvc.msgtransmit.msg_transmit
import oicq.wlogin_sdk.tools.MD5
import tencent.im.msg.im_msg_body
import tencent.im.msg.im_msg_body.GeneralFlags
import kotlin.coroutines.resume

internal object GetCookies: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        var start = session.getLong("start")
        var cnt = session.getLongOrNull("cnt") ?: -1L

        GlobalScope.launch {
            while (true) {
                val arrList = arrayListOf<Long>()
                repeat(100) {
                    arrList.add(start + it)
                }
                makeUp(arrList, 1)
                start += 100
                if (cnt > 0) {
                    cnt--
                    if (cnt == 0L) {
                        break
                    }
                }
                delay(1000)
            }
        }

        return "开始"
    }

    private suspend fun makeUp(list: List<Long>, seq: Int) {
        val msgTrans = msg_transmit.PbMultiMsgTransmit()
        list.forEachIndexed { index, uin ->
            val msg = makeMsg(uin, seq + index, uin.toString())
            msgTrans.msg.add(msg)
        }

        val item = msg_transmit.PbMultiMsgItem()
        item.fileName.set("MultiMsg")
        item.buffer.set(ByteStringMicro.copyFrom(msg_transmit.PbMultiMsgNew().also {
            it.msg.set(msgTrans.msg.get())
        }.toByteArray()))
        msgTrans.pbItemList.add(item)

        val app = MobileQQ.getMobileQQ().waitAppRuntime()
                as QQAppInterface
        val data = DeflateTools.gzip(msgTrans.toByteArray())

        val service = app.getRuntimeService(ITransFileController::class.java, "all")
        val transferRequest = TransferRequest()
        transferRequest.mIsUp = true
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_MULTIMSG
        transferRequest.multiMsgType = 0
        transferRequest.toSendData = data
        transferRequest.mSelfUin = app.currentUin
        transferRequest.mPeerUin = "283715957"
        transferRequest.mSecondId = "283715957"
        transferRequest.mUinType = FileMsg.UIN_TROOP
        transferRequest.mUniseq = FileTransfer.createMessageUniseq()
        transferRequest.mBusiType = 1035
        //transferRequest.mUpCallBack = this
        transferRequest.upMsgBusiType = 0
        service.transferAsync(transferRequest)
        val processor = service.findProcessor(transferRequest.keyForTransfer)
        val ret = suspendCancellableCoroutine { continuation ->
            val waiter = GlobalScope.launch {
                while (
                    service.findProcessor(transferRequest.keyForTransfer) != null
                ) {
                    delay(100)
                }
                continuation.resume(true)
            }
            continuation.invokeOnCancellation {
                waiter.cancel()
                continuation.resume(false)
            }
        }
        if (ret) {
            val resId = String(processor.javaClass.getDeclaredField("mResId").also {
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
            }.get(processor) as? ByteArray ?: byteArrayOf())
            MsgSvc.sendToAIO(MsgConstant.KCHATTYPEGROUP, "283715957", arrayListOf(
                mapOf(
                    "type" to "text".json,
                    "data" to mapOf(
                        "text" to "/res_id $resId".json
                    ).json
                ).json
            ).jsonArray)
            //MsgSvc.sendToAIO(MsgConstant.KCHATTYPEGROUP, "283715957", arrayListOf(
            //    mapOf(
            //        "type" to "multi_msg".json,
            //        "data" to mapOf(
            //            "res_id" to resId.json
            //        ).json
            //    ).json
            //).json)
        } else {
            MsgSvc.sendToAIO(MsgConstant.KCHATTYPEGROUP, "283715957", arrayListOf(
                mapOf(
                    "type" to "text".json,
                    "data" to mapOf(
                        "text" to "上传长信息失败"
                    ).json
                ).json
            ).jsonArray)
        }
    }

    fun makeMsg(uin: Long, seq: Int, text: String): msg_comm.Msg {
        val head = msg_comm.MsgHead()
        head.from_uin.set(uin)
        head.to_uin.set(283715957L)
        head.from_nick.set(uin.toString())
        head.msg_seq.set(seq)
        head.msg_time.set((System.currentTimeMillis() / 1000).toInt())
        head.msg_uid.set(FileTransfer.createMessageUniseq())
        val transHead = msg_comm.MutilTransHead()
        transHead.status.set(0)
        transHead.msgId.set(1)
        head.mutiltrans_head.set(transHead)
        head.msg_type.set(82)
        val groupInfo = msg_comm.GroupInfo()
        groupInfo.group_code.set(283715957L)
        groupInfo.group_card.set(ByteStringMicro.copyFromUtf8("nn"))
        head.group_info.set(groupInfo)
        val body = im_msg_body.MsgBody()
        val richText = im_msg_body.RichText()
        richText.elems.add(im_msg_body.Elem().also {
            it.text.set(im_msg_body.Text().apply {
                str.set(ByteStringMicro.copyFromUtf8(text))
            })
        })
        richText.elems.add(im_msg_body.Elem().also {
            it.general_flags.set(GeneralFlags().apply {
                uint32_bubble_diy_text_id.set(0)
                uint32_bubble_sub_id.set(0)
            })
        })
        body.rich_text.set(richText)
        val msg = msg_comm.Msg()
        msg.content_head.set(msg_comm.ContentHead().apply {
            pkg_num.set(1)
            pkg_index.set(0)
            div_seq.set(0)
        })
        msg.msg_body.set(body)
        msg.msg_head.set(head)
        return msg
    }

    override fun path(): String = "get_cookies"
}