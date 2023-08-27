package moe.fuqiuluo.remote.action.handlers

import android.os.Bundle
import com.tencent.mobileqq.data.ProfileProtocolConst
import com.tencent.mobileqq.profilecard.api.IProfileProtocolService
import moe.fuqiuluo.remote.action.ActionSession
import moe.fuqiuluo.remote.action.IActionHandler
import mqq.app.MobileQQ

internal object SetProfileCard: IActionHandler() {
    override suspend fun internalHandle(session: ActionSession): String {
        val nickName = session.getString("nickname")
        val company = session.getString("company")
        val email = session.getString("email")
        val college = session.getString("college")
        val personalNote = session.getString("personal_note")

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

    override val requiredParams: Array<String> = arrayOf("nickname", "company", "email", "college", "personal_note")

    override fun path(): String = "set_qq_profile"
}