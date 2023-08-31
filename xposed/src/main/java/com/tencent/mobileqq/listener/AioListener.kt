@file:OptIn(DelicateCoroutinesApi::class)
package com.tencent.mobileqq.listener

import com.tencent.mobileqq.pushservice.HttpPusher
import com.tencent.qqnt.helper.MessageHelper
import com.tencent.qqnt.kernel.nativeinterface.*
import com.tencent.qqnt.msg.toCQCode
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.xposed.helper.Level
import moe.fuqiuluo.xposed.helper.LogCenter
import java.util.ArrayList
import java.util.HashMap

internal object AioListener: IKernelMsgListener {
    override fun onRecvMsg(msgList: ArrayList<MsgRecord>) {
        if (msgList.isEmpty()) return

        GlobalScope.launch {
            msgList.forEach {
                handleMsg(it)
            }
        }
    }

    private suspend fun handleMsg(record: MsgRecord) {
        try {
            val rawMsg = record.elements.toCQCode(record.chatType)
            val msgHash = MessageHelper.convertMsgIdToMsgHash(record.chatType, record.msgId, record.peerUin)
            when (record.chatType) {
                MsgConstant.KCHATTYPEGROUP -> {
                    LogCenter.log("群消息(group = ${record.peerName}(${record.peerUin}), uin = ${record.senderUin}, msg = $rawMsg)")
                    HttpPusher.pushGroupMsg(record, record.elements, rawMsg, msgHash)
                }
                MsgConstant.KCHATTYPEC2C -> {
                    LogCenter.log("私聊消息(private = ${record.senderUin}, msg = $rawMsg)")
                    HttpPusher.pushPrivateMsg(record, record.elements, rawMsg, msgHash)
                }
                else -> {
                    LogCenter.log("不支持PUSH事件: ${record.chatType}")
                }
            }
        } catch (e: Throwable) {
            LogCenter.log(e.stackTraceToString(), Level.WARN)
        }
    }

    override fun onAddSendMsg(record: MsgRecord) {
        GlobalScope.launch {
            LogCenter.log(record.toCQCode())
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