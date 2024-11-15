import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.R

@Composable
fun deletePopup(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = modifier
                    .wrapContentHeight()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        modifier = modifier.size(24.dp),
                        onClick = { onDismissRequest() }
                    ) {
                        Icon(
                            modifier = modifier.height(14.dp),
                            painter = painterResource(id = R.drawable.x),
                            contentDescription = null
                        )
                    }
                }
                Icon(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(84.dp),
                    painter = painterResource(id = R.drawable.delete_dog),
                    contentDescription = null,
                )
                Text(
                    modifier = modifier.padding(PaddingValues(top = 17.dp)),
                    text = buildAnnotatedString {
                        append("회원탈퇴")
                        withStyle(style = SpanStyle(color = mainBlack)) {
                            append("를")
                        }
                    },
                    color = Color.Red
                )
                Text(
                    modifier = modifier
                        .padding(PaddingValues(bottom = 30.dp)),
                    text = "진행하시겠습니까?",
                    color = mainBlack
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onConfirmation() },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, mainBlack)
                    ) {
                        Text(
                            "확인",
                            style = Typography.labelLarge,
                            color = mainBlack
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewQuizPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        deletePopup(onConfirmation = {}) { }
    }
}
