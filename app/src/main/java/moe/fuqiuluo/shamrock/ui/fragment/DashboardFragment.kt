@file:OptIn(ExperimentalFoundationApi::class)
package moe.fuqiuluo.shamrock.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
import moe.fuqiuluo.shamrock.ui.tools.InputDialog
import moe.fuqiuluo.shamrock.ui.tools.toast


@Composable
fun DashboardFragment(
    nick: String,
    uin: String
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val preferences = ctx.getSharedPreferences("config", 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AccountCard(nick, uin)
        InformationCard(ctx)
        APIInfoCard(scope, ctx, preferences)
        FunctionCard(scope, ctx, preferences, "功能设置")
    }
}

@Composable
private fun APIInfoCard(
    scope: CoroutineScope,
    ctx: Context,
    preferences: SharedPreferences
) {
    ActionBox(
        modifier = Modifier.padding(top = 12.dp),
        painter = painterResource(id = R.drawable.round_info_24),
        title = "接口信息(双击修改)"
    ) {
        Column {
            Divider(
                modifier = Modifier,
                color = TabUnSelectedColor,
                thickness = 0.2.dp
            )

            val port = remember { mutableStateOf(preferences.getInt("port", 5700).toString()) }

            val dialogPortInputState = InputDialog(
                openDialog = remember { mutableStateOf(false) },
                title = "主动HTTP端口",
                desc = "端口范围在0~65565，并确保可用。",
                isError = remember { mutableStateOf(false) },
                text = port,
                hint = "请输入端口号",
                keyboardType = KeyboardType.Number,
                errorText = "端口范围应在0~65565",
            ) {
                it.isNotBlank() && it.toInt() in 0 .. 65565
            }
            InfoItem(
                title = "主动HTTP监听端口",
                content = port.value
            ) {
                dialogPortInputState.show(
                    confirm = {
                        val newPort = port.value.toInt()
                        preferences.edit { putInt("port", newPort) }
                        scope.toast(ctx, "也许需要重启QQ生效")
                        AppRuntime.log("设置主动HTTP监听端口为$newPort，也许需要重启QQ生效哦。")
                    },
                    cancel = {
                        scope.toast(ctx, "取消修改")
                    }
                )
            }

            val wsPort = remember { mutableStateOf(preferences.getInt("ws_port", 5800).toString()) }
            val dialogWsPortInputState = InputDialog(
                openDialog = remember { mutableStateOf(false) },
                title = "主动WebSocket端口",
                desc = "端口范围在0~65565，请确保可用。",
                isError = remember { mutableStateOf(false) },
                text = wsPort,
                hint = "请输入端口号",
                keyboardType = KeyboardType.Number,
                errorText = "端口范围应在0~65565",
            ) {
                it.isNotBlank() && it.toInt() in 0 .. 65565
            }
            InfoItem(
                title = "主动WebSocket端口",
                content = wsPort.value
            ) {
                dialogWsPortInputState.show(
                    confirm = {
                        val newPort = wsPort.value.toInt()
                        preferences.edit { putInt("ws_port", newPort) }
                        scope.toast(ctx, "重启QQ生效")
                        AppRuntime.log("设置主动WebSocket监听端口为$newPort，重启QQ生效。")
                    },
                    cancel = {
                        scope.toast(ctx, "取消修改")
                    }
                )
            }

            val webHookAddress = remember { mutableStateOf(preferences.getString("http_addr", "shamrock.moe:80")!!) }
            val dialogWebHookAddressInputState = InputDialog(
                openDialog = remember { mutableStateOf(false) },
                title = "回调HTTP地址",
                desc = "无需携带’http://‘，例如：shamrock.moe:80。",
                isError = remember { mutableStateOf(false) },
                text = webHookAddress,
                hint = "shamrock.moe:80",
                keyboardType = KeyboardType.Text,
                errorText = "输入的地址不合法",
            ) {
                it.isNotBlank() && !it.startsWith("http://") && !it.startsWith("https://") && !it.startsWith("ws://")
            }
            InfoItem(
                title = "被动HTTP回调地址",
                content = webHookAddress.value
            ) {
                dialogWebHookAddressInputState.show(
                    confirm = {
                        preferences.edit { putString("http_addr", webHookAddress.value) }
                        scope.toast(ctx, "重启QQ生效")
                        AppRuntime.log("设置回调HTTP地址为[${webHookAddress.value}]，重启QQ生效。")
                    },
                    cancel = {
                        scope.toast(ctx, "取消修改")
                    }
                )
            }

            val wsAddress = remember { mutableStateOf(preferences.getString("ws_addr", "shamrock.moe:81")!!) }
            val dialogWsAddressInputState = InputDialog(
                openDialog = remember { mutableStateOf(false) },
                title = "被动WebSocket地址",
                desc = "无需携带‘ws://’，例如：shamrock.moe:81。",
                isError = remember { mutableStateOf(false) },
                text = wsAddress,
                hint = "shamrock.moe:81",
                keyboardType = KeyboardType.Text,
                errorText = "输入的地址不合法",
            ) {
                it.isNotBlank() && !it.startsWith("http://") && !it.startsWith("https://") && !it.startsWith("ws://")
            }
            InfoItem(
                title = "被动WebSocket地址",
                content = wsAddress.value
            ) {
                dialogWsAddressInputState.show(
                    confirm = {
                        preferences.edit { putString("ws_addr", wsAddress.value) }
                        scope.toast(ctx, "重启QQ生效")
                        AppRuntime.log("设置被动WebSocket服务端地址为[${wsAddress.value}]，重启QQ生效。")
                    },
                    cancel = {
                        scope.toast(ctx, "取消修改")
                    }
                )
            }

            InfoItem(
                title = "累计调用次数",
                content = AppRuntime.requestCount.intValue.toString()
            )
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
                scope.toast(ctx, "重启QQ生效")
            }

            Function(
                title = "HTTP回调",
                desc = "OneBot标准的HTTPAPI回调，Shamrock作为Client。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("webhook", false)
            ) {
                preferences.edit { putBoolean("webhook", it) }
                scope.toast(ctx, "重启QQ生效")
            }

            Function(
                title = "消息格式为CQ码",
                desc = "HTTPAPI回调的消息格式，关闭则为消息段。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("use_cqcode", false)
            ) {
                preferences.edit { putBoolean("use_cqcode", it) }
                scope.toast(ctx, "重启QQ生效")
            }

            Function(
                title = "主动WebSocket",
                desc = "OneBot标准WebSocket，Shamrock作为Server。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("ws", false)
            ) {
                preferences.edit { putBoolean("ws", it) }
                scope.toast(ctx, "重启QQ生效")
            }

            Function(
                title = "被动WebSocket",
                desc = "OneBot标准WebSocket，Shamrock作为Client。",
                descColor = TabSelectedColor,
                isSwitch = preferences.getBoolean("ws_client", false)
            ) {
                preferences.edit { putBoolean("ws_client", it) }
                scope.toast(ctx, "重启QQ生效")

            }

            Function(
                title = "专业级接口",
                desc = "如果你不知道你在做什么，请不要开启本功能。",
                descColor = Color.Red,
                isSwitch = preferences.getBoolean("pro_api", false)
            ) {
                preferences.edit { putBoolean("pro_api", it) }
                scope.toast(ctx, "重启QQ生效")
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
private fun InformationCard(ctx: Context) {
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

            InfoItem(title = "设备", content = "${Build.BRAND} ${Build.MODEL}") {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val mClipData = ClipData.newPlainText("Label", it)
                cm.setPrimaryClip(mClipData)
            }

            InfoItem(title = "系统架构", content = Build.SUPPORTED_ABIS.joinToString())
        }
    }
}

@Composable
private fun InfoItem(
    modifier: Modifier = Modifier,
    titleColor: Color = Color(0xFF5A5A5A),
    contentColor: Color = Color(0xFF5A5A5A),
    title: String,
    content: String,
    doubleClick: ((String) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .absolutePadding(left = 8.dp, right = 8.dp, top = 12.dp, bottom = 0.dp)
            .fillMaxWidth()
            .combinedClickable(onDoubleClick = {
                doubleClick?.invoke(content)
            }) {
               // nothing
            }
        ,
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