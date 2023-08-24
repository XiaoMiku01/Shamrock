package moe.fuqiuluo.xposed.actions

import android.content.Context
import moe.fuqiuluo.xposed.actions.IAction

internal class HookForDebug: IAction {
    override fun invoke(ctx: Context) {
        // MessageHelper.hookSendMessageOldChannel()
    }
}