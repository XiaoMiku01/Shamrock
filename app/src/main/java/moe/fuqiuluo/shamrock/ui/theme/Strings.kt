@file:Suppress(
    "SpellCheckingInspection", "unused", "PropertyName",
    "ClassName", "NonAsciiCharacters"
)
package moe.fuqiuluo.shamrock.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import moe.fuqiuluo.shamrock.R

private val LocalStringDefault = Default()
private val LocalString2B = Chūnibyō()

val LocalString: VarString
    @ReadOnlyComposable
    @Composable
    get() {
        val ctx = LocalContext.current
        val sharedPreferences = ctx.getSharedPreferences("config", 0)
        return if (!sharedPreferences.getBoolean("2B", false)) {
            LocalStringDefault
        } else {
            LocalString2B
        }
    }

private open class Chūnibyō: Default() {
    init {
        TitlesWithIcon = arrayOf(
            "玄天" to R.drawable.round_home_24,
            "天穹" to R.drawable.round_dashboard_24,
            "无极" to R.drawable.round_monitor_heart_24,
            "飘渺" to R.drawable.round_logo_dev_24
        )
        frameworkYes = "仙路已通"
        frameworkNo = "鬼怪横行"
        frameworkYesLite = "五行已备"
        frameworkNoLite = "需待东风"
        legalWarning = "白榆，北辰，曜魄，应星，云川当方位不乱，即可作于无极之域。\n" +
                "执明起，至除免于灾祸。\n" +
                "元冥浩浩，非凡不可动之。"
        labWarning = "寒酥降矣，梅熟日久，莫不可测。"
    }
}

private open class Default: VarString(
    TitlesWithIcon = arrayOf(
        "主页" to R.drawable.round_home_24,
        "状态" to R.drawable.round_dashboard_24,
        "日志" to R.drawable.round_monitor_heart_24,
        "Lab" to R.drawable.round_logo_dev_24
    ), "框架已激活", "框架未激活",
    "已激活", "未激活",
    legalWarning = "该模块仅适用于目标版本8.9.68及以上的版本。\n" +
            "同时声明本项目仅用于学习与交流，请于24小时内删除。\n" +
            "同时开源贡献者均享受免责条例。",
    labWarning = "实验室功能，可能会导致出乎意料的BUG!",
    "日志"
)

open class VarString(
    var TitlesWithIcon: Array<Pair<String, Int>>,
    var frameworkYes: String,
    var frameworkNo: String,

    var frameworkYesLite: String,
    var frameworkNoLite: String,

    var legalWarning: String,

    var labWarning: String,

    var logTitle: String
)
