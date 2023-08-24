package moe.fuqiuluo.http.action.handlers

import android.os.Bundle
import com.tencent.mobileqq.data.ProfileProtocolConst
import com.tencent.mobileqq.profilecard.api.IProfileProtocolService
import moe.fuqiuluo.http.action.ActionSession
import moe.fuqiuluo.http.action.IActionHandler
import mqq.app.MobileQQ

internal object SetProfileCard: IActionHandler() {
    override suspend fun handle(session: ActionSession): String {
        val nickName = session.getStringOrNull("nickname") ?: return noParam("nickname")
        val company = session.getStringOrNull("company") ?: return noParam("company")
        val email = session.getStringOrNull("email") ?: return noParam("email")
        val college = session.getStringOrNull("college") ?: return noParam("college")
        val personalNote = session.getStringOrNull("personal_note") ?: return noParam("personal_note")

        val birthday = session.getIntOrNull("birthday")
        val age = session.getIntOrNull("age")

        val bundle = Bundle()
        val service = MobileQQ.getMobileQQ().waitAppRuntime()
            .getRuntimeService(IProfileProtocolService::class.java, "all")
        bundle.putString(ProfileProtocolConst.KEY_NICK, nickName)
        bundle.putString(ProfileProtocolConst.KEY_COMPANY, company)
        bundle.putString(ProfileProtocolConst.KEY_EMAIL, email)
        bundle.putString(ProfileProtocolConst.KEY_COLLEGE, college)
        bundle.putString(ProfileProtocolConst.KEY_PERSONAL_NOTE, personalNote)

        if (birthday != null) {
            bundle.putInt(ProfileProtocolConst.KEY_BIRTHDAY, birthday.toInt())
        }
        if (age != null) {
            bundle.putInt(ProfileProtocolConst.KEY_AGE, age.toInt())
        }

        service.setProfileDetail(bundle)
        return ok("设置成功")
    }

    override fun path(): String = "set_qq_profile"
}