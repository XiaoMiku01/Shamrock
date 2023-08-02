package moe.fuqiuluo.shamrock.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.shamrock.R
import moe.fuqiuluo.shamrock.ui.app.AppRuntime
import moe.fuqiuluo.shamrock.ui.app.Level
import moe.fuqiuluo.shamrock.ui.theme.TabSelectedColor
import moe.fuqiuluo.shamrock.ui.theme.TabUnSelectedColor

@Composable
fun DashboardFragment(
    nick: String,
    uin: String
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val preferences = ctx.getSharedPreferences("config", 0)

    Surface(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AccountCard(nick, uin)
            InformationCard()
            FunctionCard(scope, ctx, preferences, "功能设置")
        }
    }
}

@Composable
private fun FunctionCard(
    scope: CoroutineScope,
    ctx: Context,
    preferences: SharedPreferences,
    title: String
) {
    ActionBox(
        modifier = Modifier.padding(top = 12.dp),
        painter = painterResource(id = R.drawable.round_api_24),
        title = title
    ) {
        Column {
            Divider(
                modifier = Modifier,
                color = TabUnSelectedColor,
                thickness = 0.2.dp
            )

            Function(
                title = "强制平板模式",
                desc = "强制QQ使用平板模式，实现共存登录。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("tablet", false)
            ) {
                preferences.edit { putBoolean("tablet", it) }
                scope.launch { Toast.makeText(ctx, "重启QQ生效", Toast.LENGTH_SHORT).show() }
            }

            Function(
                title = "HTTP回调",
                desc = "OneBot标准的HTTPAPI回调，Shamrock作为Client。",
                descColor = TabSelectedColor,
                isSwitch = !preferences.getString("webhook", "").isNullOrBlank()
            ) {

                //preferences.edit { putBoolean("webhook", it) }
                scope.launch { Toast.makeText(ctx, "重启QQ生效", Toast.LENGTH_SHORT).show() }

            }

            Function(
                title = "主动WebSocket",
                desc = "OneBot标准WebSocket，Shamrock作为Server。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("ws", false)
            ) {
                preferences.edit { putBoolean("ws", it) }
                scope.launch { Toast.makeText(ctx, "重启QQ生效", Toast.LENGTH_SHORT).show() }

            }

            Function(
                title = "被动WebSocket",
                desc = "OneBot标准WebSocket，Shamrock作为Client。",
                descColor = TabSelectedColor,
                isSwitch = !preferences.getString("ws_api", "").isNullOrBlank()
            ) {

                scope.launch { Toast.makeText(ctx, "重启QQ生效", Toast.LENGTH_SHORT).show() }

            }

            Function(
                title = "专业级接口",
                desc = "如果你不知道你在做什么，请不要开启本功能。",
                descColor = Color.Red,
                isSwitch = preferences.getBoolean("pro_api", false)
            ) {
                preferences.edit { putBoolean("pro_api", it) }
                scope.launch { Toast.makeText(ctx, "重启QQ生效", Toast.LENGTH_SHORT).show() }
                AppRuntime.log("专业级API = $it", Level.WARN)
            }
        }
    }
}

@Composable
private fun Function(
    title: String,
    desc: String? = null,
    descColor: Color? = null,
    isSwitch: Boolean,
    onClick: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .absolutePadding(left = 8.dp, right = 8.dp, top = 12.dp, bottom = 0.dp)
    ) {
        if (desc != null && descColor != null) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = desc,
                color = descColor,
                fontSize = 11.sp
            )
        }
        ActionSwitch(
            text = title,
            isSwitch = isSwitch
        ) {
            onClick(it)
        }
    }
}

@Composable
private fun InformationCard() {
    ActionBox(
        modifier = Modifier.padding(top = 12.dp),
        painter = painterResource(id = R.drawable.round_info_24),
        title = "数据信息"
    ) {
        Column {
            Divider(
                modifier = Modifier,
                color = TabUnSelectedColor,
                thickness = 0.2.dp
            )

            InfoItem(
                title = "系统版本",
                content =  "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            )

            InfoItem(title = "设备", content = "${Build.BRAND} ${Build.MODEL}")

            InfoItem(title = "系统架构", content = Build.SUPPORTED_ABIS.joinToString())

            InfoItem(
                title = "累计调用次数",
                content = "114514"
            )
        }
    }
}

@Composable
private fun InfoItem(
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF5A5A5A),
    contentColor: Color = Color(0xFF5A5A5A),
    title: String,
    content: String
) {
    Row(
        modifier = Modifier
            .absolutePadding(left = 8.dp, right = 8.dp, top = 12.dp, bottom = 0.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = titleColor
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = content,
                fontSize = 14.sp,
                color = contentColor
            )
        }
    }
}

@Composable
private fun AccountCard(
    nick: String,
    uin: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
                ), shape = RoundedCornerShape(12.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = kotlin.runCatching {
            rememberAsyncImagePainter(model = ImageRequest.Builder(LocalContext.current)
                .data("https://q.qlogo.cn/g?b=qq&nk=$uin&s=100")
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build())
        }.onSuccess {
            when(it.state){
                is AsyncImagePainter.State.Success ->{

                }
                is AsyncImagePainter.State.Loading ->{
                }
                is AsyncImagePainter.State.Error ->{
                    AppRuntime.log("头像拉取失败，请检查网络连接。", Level.ERROR)
                }
                else -> {}
            }
        }.getOrDefault(painterResource(id = R.drawable.ic_letter_q))

        Icon(
            modifier = Modifier
                .padding(
                    start = 15.dp,
                    end = 5.dp
                )
                .width(45.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(36.dp))
            ,
            painter = painter,
            contentDescription = "HeadLogo",
            tint = Color.Unspecified
        )
        Column(
            modifier = Modifier
                .padding(20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = nick,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "QQ号：$uin",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Preview
@Composable
private fun PreViewDashBoard() {
    DashboardFragment("测试昵称", "100001")
}