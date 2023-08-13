@file:OptIn(DelicateCoroutinesApi::class)

package moe.fuqiuluo.http.action.helper

import com.tencent.mobileqq.transfile.BaseTransProcessor
import com.tencent.mobileqq.transfile.FileMsg
import com.tencent.mobileqq.transfile.TransferRequest
import com.tencent.mobileqq.transfile.api.ITransFileController
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tools.MD5
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.random.Random

internal object HighwayHelper {

    suspend fun transTroopMessage(groupId: String, file: File): Boolean {
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        val service = runtime.getRuntimeService(ITransFileController::class.java, "all") as ITransFileController
        val transferRequest = TransferRequest()
        transferRequest.needSendMsg = false
        transferRequest.mSelfUin = runtime.account
        transferRequest.mPeerUin = groupId
        transferRequest.mSecondId = runtime.currentAccountUin
        transferRequest.mUinType = FileMsg.UIN_TROOP
        transferRequest.mFileType = FileMsg.TRANSFILE_TYPE_PIC
        transferRequest.mUniseq = createMessageUniseq(System.currentTimeMillis())
        transferRequest.mIsUp = true
        transferRequest.mBusiType = 1030
        transferRequest.mMd5 = MD5.getFileMD5(file)
        transferRequest.mLocalPath = file.absolutePath
        val picUpExtraInfo = TransferRequest.PicUpExtraInfo()
        picUpExtraInfo.mIsRaw = true
        transferRequest.mPicSendSource = 8
        transferRequest.mExtraObj = picUpExtraInfo
        if(service.transferAsync(transferRequest)) {
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
    private fun createMessageUniseq(time: Long): Long {
        var uniseq = (time / 1000).toInt().toLong()
        uniseq = uniseq shl 32 or abs(Random.nextInt()).toLong()
        return uniseq
    }

}