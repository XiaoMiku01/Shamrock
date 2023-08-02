package moe.fuqiuluo.shamrock.ui.tools

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.fuqiuluo.shamrock.ui.theme.ShamrockTheme
import moe.fuqiuluo.shamrock.ui.theme.TabSelectedColor
import moe.fuqiuluo.shamrock.ui.theme.TabUnSelectedColor

@Composable
fun NoticeTextDialog(
    openDialog: MutableState<Boolean>,
    title: String,
    text: String
) {
    AlertDialog(
        shape = RoundedCornerShape(12.dp),
        onDismissRequest = {
            openDialog.value = false
        },
        title = {
            Text(
                text = title,
                fontSize = 16.sp,
                color = TabSelectedColor
            )
        },
        text = {
            Text(
                text,
                fontSize = 14.sp,
                color = TabUnSelectedColor
            )
        },
        confirmButton = {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    openDialog.value = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                )
            ) {
                Text("我知道了")
            }
        },
    )
}

@Preview
@Composable
private fun NoticeDialogPreView() {
    ShamrockTheme {
        NoticeTextDialog(openDialog = remember {
            mutableStateOf(true)
        }, "Notice", "Text")
    }
}