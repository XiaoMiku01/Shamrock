package moe.fuqiuluo.http.action.handlers

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.data.Card
import com.tencent.mobileqq.profilecard.api.IProfileDataService
import com.tencent.mobileqq.profilecard.api.IProfileProtocolService
import com.tencent.mobileqq.profilecard.observer.ProfileCardObserver
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString
import moe.fuqiuluo.xposed.tools.toInnerValuesString
import mqq.app.MobileQQ
import java.util.concurrent.ArrayBlockingQueue

internal object GetProfileCard: IActionHandler() {
    override fun handle(session: ActionSession): String {
        if (!session.has("user_id")) {
            return badParam("user_id")
        }
        val uin = session.getString("user_id")
        val refresh = session.getBooleanOrDefault("refresh", false)
        val runtime = MobileQQ.getMobileQQ().waitAppRuntime()
        if (runtime !is AppInterface)
            return logic("AppRuntime cannot cast to AppInterface")

        val profileDataService = runtime
            .getRuntimeService(IProfileDataService::class.java, "all")
        var card: Card? = profileDataService.getProfileCard(uin, true)
        if (refresh || !card.ok()) {
            card = refreshProfileCard(uin, runtime, profileDataService)
        }
        if (!card.ok()) {
            return logic("get profilecard error, please check your user_id or network")
        }
        requireNotNull(card)

        return resultToString(true, Status.Ok, MyCard(
            uin = card.uin,
            name = card.strNick,
            mail = card.strShowName,
            remark = card.strReMark.let { if (it.isNullOrBlank()) card.strAutoRemark else it },
            findMethod = card.addSrcName,
            displayName = card.strContactName,
            maxVoteCnt = card.bAvailVoteCnt,
            haveVoteCnt = card.bHaveVotedCnt,
            vipList = arrayListOf<VipInfo>().apply {
                if (card.bQQVipOpen == 1.toByte()) {
                    add(VipInfo(VipType.QQ_VIP, card.iQQVipLevel, card.iQQVipType, 0))
                }
                if (card.bSuperQQOpen == 1.toByte()) {
                    add(VipInfo(VipType.SUPER_QQ, card.iSuperQQLevel, card.iSuperQQType, 0))
                }
                if (card.bSuperVipOpen == 1.toByte()) {
                    add(VipInfo(VipType.SUPER_VIP, card.iSuperVipLevel, card.iSuperVipType, card.lSuperVipTemplateId))
                }
                if (card.bHollywoodVipOpen == 1.toByte()) {
                    add(VipInfo(VipType.QQ_VIDEO, card.iHollywoodVipLevel, card.iHollywoodVipType, 0))
                }
                if (card.bBigClubVipOpen == 1.toByte()) {
                    add(VipInfo(VipType.BIG_VIP, card.iBigClubVipLevel, card.iBigClubVipType, card.lBigClubTemplateId))
                }
                if (card.isYellowDiamond || card.isSuperYellowDiamond) {
                    add(VipInfo(VipType.YELLOW_VIP, card.yellowLevel, 0, 0))
                }
            },
            hobbyEntry = card.hobbyEntry,
            level = card.iQQLevel,
            birthday = card.lBirthday,
            loginDay = card.lLoginDays,
            voteCnt = card.lVoteCount,
            qid = card.qid,
            schoolVerified = card.schoolVerifiedFlag,
            location = Location(
                card.strCity, card.strCompany, card.strCountry, card.strProvince, card.strHometownDesc, card.strSchool
            ),
        ))
    }

    private fun refreshProfileCard(uin: String, app: AppInterface, dataService: IProfileDataService): Card? {
        val queue = ArrayBlockingQueue<Card?>(1)
        app.addObserver(object: ProfileCardObserver() {
            override fun onGetProfileCard(success: Boolean, obj: Any) {
                if (!success || obj !is Card) {
                    queue.put(null)
                } else {
                    queue.put(obj)
                    dataService.saveProfileCard(obj)
                }
                app.removeObserver(this)
            }
        })
        app.getRuntimeService(IProfileProtocolService::class.java, "all")
            .requestProfileCard(app.currentUin, uin, 12, 0L, 0.toByte(), 0L, 0L, null, "", 0L, 10004, null, 0.toByte())
        return queue.take()
    }

    private fun Card?.ok(): Boolean {
        return this != null && !strNick.isNullOrBlank()
    }

    @Serializable
    data class MyCard(
        @SerialName("user_id") val uin: String,
        @SerialName("user_name") val name: String,
        @SerialName("user_displayname") val displayName: String,
        @SerialName("user_remark") val remark: String,
        val mail: String,
        @SerialName("find_method") val findMethod: String,

        @SerialName("max_vote_cnt") val maxVoteCnt: Short,
        @SerialName("have_vote_cnt") val haveVoteCnt: Short,

        @SerialName("vip_list") val vipList: List<VipInfo>,

        @SerialName("hobby_entry") val hobbyEntry: String,

        val level: Int,
        val birthday: Long,
        @SerialName("login_day") val loginDay: Long,

        @SerialName("vote_cnt") val voteCnt: Long,

        val qid: String,
        @SerialName("is_school_verified") val schoolVerified: Boolean,

        val location: Location,
    )

    @Serializable
    data class Location(
        val city: String,
        val company: String,
        val country: String,
        val province: String,
        val hometown: String,
        val school: String,

    )

    @Serializable
    data class VipInfo(
        val type: VipType,
        val level: Int,
        @SerialName("vip_type") val vipType: Int,
        @SerialName("template_id") val templateId: Long
    )

    enum class VipType {
        QQ_VIP,
        SUPER_QQ,
        SUPER_VIP,
        QQ_VIDEO,
        QQ_READING,
        BIG_VIP,
        YELLOW_VIP
    }

    override fun path(): String = "get_user_info"
}