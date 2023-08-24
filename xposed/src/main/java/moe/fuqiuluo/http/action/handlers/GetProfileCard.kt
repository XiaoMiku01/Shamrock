package moe.fuqiuluo.http.action.handlers

import com.tencent.mobileqq.data.Card
import kotlinx.coroutines.sync.Mutex
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import com.tencent.mobileqq.data.Location
import com.tencent.mobileqq.data.ProfileCard
import com.tencent.mobileqq.data.VipInfo
import com.tencent.mobileqq.data.VipType
import com.tencent.qqnt.protocol.CardSvc
import moe.fuqiuluo.http.entries.Status
import moe.fuqiuluo.http.entries.resultToString

internal object GetProfileCard: IActionHandler() {
    private val refreshLock = Mutex() // 防止重复注册监视器导致错误

    override suspend fun handle(session: ActionSession): String {
        if (!session.has("user_id")) {
            return noParam("user_id")
        }
        val uin = session.getString("user_id")
        val refresh = session.getBooleanOrDefault("refresh", false)

        var card: Card? = CardSvc.getProfileCard(uin)
        if (refresh || !card.ok()) {
            card = CardSvc.refreshAndGetProfileCard(uin)
        }
        if (!card.ok()) {
            return logic("get profilecard error, please check your user_id or network")
        }
        requireNotNull(card)

        return resultToString(true, Status.Ok, ProfileCard(
            uin = card.uin,
            name = card.strNick,
            mail = card.strShowName ?: card.strEmail ?: "",
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
            cookie = card.vCookies
        ))
    }

    private fun Card?.ok(): Boolean {
        return this != null && !strNick.isNullOrBlank()
    }

    override fun path(): String = "get_user_info"
}