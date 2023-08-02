@file:OptIn(DelicateCoroutinesApi::class)
package com.tencent.mobileqq.listener

import com.tencent.qqnt.kernel.nativeinterface.BroadcastHelperTransNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.ContactMsgBoxInfo
import com.tencent.qqnt.kernel.nativeinterface.CustomWithdrawConfig
import com.tencent.qqnt.kernel.nativeinterface.DevInfo
import com.tencent.qqnt.kernel.nativeinterface.DownloadRelateEmojiResultInfo
import com.tencent.qqnt.kernel.nativeinterface.EmojiNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.EmojiResourceInfo
import com.tencent.qqnt.kernel.nativeinterface.FileTransNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.FirstViewDirectMsgNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.FirstViewGroupGuildInfo
import com.tencent.qqnt.kernel.nativeinterface.FreqLimitInfo
import com.tencent.qqnt.kernel.nativeinterface.GroupFileListResult
import com.tencent.qqnt.kernel.nativeinterface.GroupGuildNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.GroupItem
import com.tencent.qqnt.kernel.nativeinterface.HitRelatedEmojiWordsResult
import com.tencent.qqnt.kernel.nativeinterface.IKernelMsgListener
import com.tencent.qqnt.kernel.nativeinterface.ImportOldDbMsgNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.InputStatusInfo
import com.tencent.qqnt.kernel.nativeinterface.KickedInfo
import com.tencent.qqnt.kernel.nativeinterface.MsgAbstract
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.kernel.nativeinterface.MsgSetting
import com.tencent.qqnt.kernel.nativeinterface.RecvdOrder
import com.tencent.qqnt.kernel.nativeinterface.RelatedWordEmojiInfo
import com.tencent.qqnt.kernel.nativeinterface.SearchGroupFileResult
import com.tencent.qqnt.kernel.nativeinterface.TabStatusInfo
import com.tencent.qqnt.kernel.nativeinterface.TempChatInfo
import com.tencent.qqnt.kernel.nativeinterface.UnreadCntInfo
import de.robv.android.xposed.XposedBridge
import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.actions.impl.GlobalUi
import moe.fuqiuluo.xposed.tools.GlobalClient
import mqq.app.MobileQQ
import java.util.ArrayList
import java.util.HashMap

object AIOMSGListener: IKernelMsgListener {
    override fun onRecvMsg(msgList: ArrayList<MsgRecord>) {
        val msg = msgList.first() // 暂时忽略多消息非一包的信息
        if (msg.chatType == MsgConstant.KCHATTYPEGROUP) {
            if (msg.senderUin == 0L) return // 自己发的消息，不看


            val ctx = MobileQQ.getContext() // 获取腾讯的系统上下文

            GlobalUi.post {
                // 这里可以执行ui操作
            }

            val sharedPreferences = ctx.getSharedPreferences("shamrock_config", 0)

            // val 是否开启HTTP回调 = sharedPreferences.getBoolean("http", false)
            // val HTTP回调地址 = sharedPreferences.getString("http_addr", "")

            /* 按照名字你可以获取到对应数据
            putBoolean(  "tablet",    it.getBooleanExtra("tablet", false)) // 强制平板模式
                putInt(      "port",      it.getIntExtra("port", 5700)) // 主动HTTP端口
                putBoolean(  "ws",        it.getBooleanExtra("ws", false)) // 主动WS开关
                putInt(      "ws_port",   it.getIntExtra("port", 5700)) // 主动WS端口
                putBoolean(  "http",      it.getBooleanExtra("http", false)) // HTTP回调开关
                putString(   "http_addr", it.getStringExtra("http_addr")) // WebHook回调地址
                putBoolean(  "ws_client", it.getBooleanExtra("ws_client", false)) // 被动WS开关
                putString(   "ws_addr",   it.getStringExtra("ws_addr")) // 被动WS地址
                putBoolean(  "pro_api",   it.getBooleanExtra("pro_api", false)) // 开发调试API开关
             */

            // GlobalClient.get("") 直接写会报红
            GlobalScope.launch {
                // 百度一下如何使用ktor client发送请求什么的吧
                val respond = GlobalClient.get("http://baidu.com") // 发送应该get请求喏

            }
        }
    }

    override fun onRecvMsgSvrRspTransInfo(
        j2: Long,
        contact: Contact?,
        i2: Int,
        i3: Int,
        str: String?,
        bArr: ByteArray?
    ) {
        XposedBridge.log("onRecvMsgSvrRspTransInfo($j2, $contact, $i2, $i3, $str)")
    }

    override fun onRecvOnlineFileMsg(arrayList: ArrayList<MsgRecord>?) {

    }

    override fun onRecvS2CMsg(arrayList: ArrayList<Byte>?) {
        XposedBridge.log("onRecvS2CMsg(${arrayList.toString()})")
    }

    override fun onRecvSysMsg(arrayList: ArrayList<Byte>?) {
        XposedBridge.log("onRecvSysMsg(${arrayList.toString()})")
    }

    override fun onAddSendMsg(msgRecord: MsgRecord?) {
        XposedBridge.log("onAddSendMsg($msgRecord)")
    }

    override fun onBroadcastHelperDownloadComplete(broadcastHelperTransNotifyInfo: BroadcastHelperTransNotifyInfo?) {

    }

    override fun onBroadcastHelperProgerssUpdate(broadcastHelperTransNotifyInfo: BroadcastHelperTransNotifyInfo?) {

    }

    override fun onChannelFreqLimitInfoUpdate(
        contact: Contact?,
        z: Boolean,
        freqLimitInfo: FreqLimitInfo?
    ) {

    }

    override fun onContactUnreadCntUpdate(hashMap: HashMap<Int, HashMap<String, UnreadCntInfo>>?) {

    }

    override fun onCustomWithdrawConfigUpdate(customWithdrawConfig: CustomWithdrawConfig?) {

    }

    override fun onDraftUpdate(contact: Contact?, arrayList: ArrayList<MsgElement>?, j2: Long) {

    }

    override fun onEmojiDownloadComplete(emojiNotifyInfo: EmojiNotifyInfo?) {

    }

    override fun onEmojiResourceUpdate(emojiResourceInfo: EmojiResourceInfo?) {

    }

    override fun onFeedEventUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {

    }

    override fun onFileMsgCome(arrayList: ArrayList<MsgRecord>?) {

    }

    override fun onFirstViewDirectMsgUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {

    }

    override fun onFirstViewGroupGuildMapping(arrayList: ArrayList<FirstViewGroupGuildInfo>?) {

    }

    override fun onGrabPasswordRedBag(
        i2: Int,
        str: String?,
        i3: Int,
        recvdOrder: RecvdOrder?,
        msgRecord: MsgRecord?
    ) {

    }

    override fun onGroupFileInfoAdd(groupItem: GroupItem?) {

    }

    override fun onGroupFileInfoUpdate(groupFileListResult: GroupFileListResult?) {

    }

    override fun onGroupGuildUpdate(groupGuildNotifyInfo: GroupGuildNotifyInfo?) {

    }

    override fun onGroupTransferInfoAdd(groupItem: GroupItem?) {

    }

    override fun onGroupTransferInfoUpdate(groupFileListResult: GroupFileListResult?) {

    }

    override fun onHitCsRelatedEmojiResult(downloadRelateEmojiResultInfo: DownloadRelateEmojiResultInfo?) {

    }

    override fun onHitEmojiKeywordResult(hitRelatedEmojiWordsResult: HitRelatedEmojiWordsResult?) {

    }

    override fun onHitRelatedEmojiResult(relatedWordEmojiInfo: RelatedWordEmojiInfo?) {

    }

    override fun onImportOldDbProgressUpdate(importOldDbMsgNotifyInfo: ImportOldDbMsgNotifyInfo?) {

    }

    override fun onInputStatusPush(inputStatusInfo: InputStatusInfo?) {

    }

    override fun onKickedOffLine(kickedInfo: KickedInfo?) {

    }

    override fun onLineDev(arrayList: ArrayList<DevInfo>?) {

    }

    override fun onLogLevelChanged(j2: Long) {

    }

    override fun onMsgAbstractUpdate(arrayList: ArrayList<MsgAbstract>?) {

    }

    override fun onMsgBoxChanged(arrayList: ArrayList<ContactMsgBoxInfo>?) {

    }

    override fun onMsgDelete(contact: Contact?, arrayList: ArrayList<Long>?) {

    }

    override fun onMsgEventListUpdate(hashMap: HashMap<String, ArrayList<Long>>?) {

    }

    override fun onMsgInfoListAdd(arrayList: ArrayList<MsgRecord>?) {

    }

    override fun onMsgInfoListUpdate(arrayList: ArrayList<MsgRecord>?) {

    }

    override fun onMsgQRCodeStatusChanged(i2: Int) {

    }

    override fun onMsgRecall(i2: Int, str: String?, j2: Long) {

    }

    override fun onMsgSecurityNotify(msgRecord: MsgRecord?) {

    }

    override fun onMsgSettingUpdate(msgSetting: MsgSetting?) {

    }

    override fun onNtFirstViewMsgSyncEnd() {

    }

    override fun onNtMsgSyncEnd() {

    }

    override fun onNtMsgSyncStart() {

    }

    override fun onReadFeedEventUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {

    }

    override fun onRecvGroupGuildFlag(i2: Int) {

    }

    override fun onRecvUDCFlag(i2: Int) {

    }

    override fun onRichMediaDownloadComplete(fileTransNotifyInfo: FileTransNotifyInfo?) {

    }

    override fun onRichMediaProgerssUpdate(fileTransNotifyInfo: FileTransNotifyInfo?) {

    }

    override fun onRichMediaUploadComplete(fileTransNotifyInfo: FileTransNotifyInfo?) {

    }

    override fun onSearchGroupFileInfoUpdate(searchGroupFileResult: SearchGroupFileResult?) {

    }

    override fun onSendMsgError(j2: Long, contact: Contact?, i2: Int, str: String?) {

    }

    override fun onSysMsgNotification(i2: Int, j2: Long, j3: Long, arrayList: ArrayList<Byte>?) {

    }

    override fun onTempChatInfoUpdate(tempChatInfo: TempChatInfo?) {

    }

    override fun onUnreadCntAfterFirstView(hashMap: HashMap<Int, ArrayList<UnreadCntInfo>>?) {

    }

    override fun onUnreadCntUpdate(hashMap: HashMap<Int, ArrayList<UnreadCntInfo>>?) {

    }

    override fun onUserChannelTabStatusChanged(z: Boolean) {

    }

    override fun onUserOnlineStatusChanged(z: Boolean) {

    }

    override fun onUserTabStatusChanged(arrayList: ArrayList<TabStatusInfo>?) {

    }

    override fun onlineStatusBigIconDownloadPush(i2: Int, j2: Long, str: String?) {

    }

    override fun onlineStatusSmallIconDownloadPush(i2: Int, j2: Long, str: String?) {

    }
}