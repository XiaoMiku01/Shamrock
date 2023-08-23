package moe.fuqiuluo.shamrock.ui.fragment

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.fuqiuluo.shamrock.ui.app.AppRuntime
import moe.fuqiuluo.shamrock.ui.app.Logger
import java.lang.StringBuilder
import java.util.Collections.EMPTY_LIST

@Composable
fun LogFragment(
    logger: Logger
) {
    //val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        NoticeBox(text = "日志仅保留最新的${AppRuntime.maxLogSize}条，超出部分会自动删除，如有需要请做好保留。")

        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxSize()
                .indication(remember { MutableInteractionSource() }, null)
                .background(
                    color = Color(0xFFf4f4f4),
                    shape = RoundedCornerShape(12.dp)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = 10.dp,
                        end = 10.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                @Suppress("UNCHECKED_CAST") val text = remember {
                    mutableStateOf(
                        AnnotatedString(
                            text = "",
                            spanStyles = EMPTY_LIST as List<AnnotatedString.Range<SpanStyle>>,
                            paragraphStyles = EMPTY_LIST as List<AnnotatedString.Range<ParagraphStyle>>
                        )
                    )
                }
                val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
                val paragraphStyles = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
                logger.logRanges.forEach {
                    spanStyles.add(AnnotatedString.Range(
                        SpanStyle(
                            color = it.level.color
                        ), it.start, it.end
                    ))
                    paragraphStyles.add(AnnotatedString.Range(
                        ParagraphStyle(
                            textAlign = TextAlign.Start
                        ), it.start, it.end
                    ))
                }
                text.value = AnnotatedString(
                    text = logger.logCache.value.toString(),
                    spanStyles = spanStyles,
                    paragraphStyles = paragraphStyles
                )

                SelectionContainer {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text.value,
                        fontSize = 12.sp,
                        color = Color(0xff6c6c6c)
                    )
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun LogPreview() {
    LogFragment(
        Logger(mutableStateOf(StringBuilder()), mutableListOf())
    )
}