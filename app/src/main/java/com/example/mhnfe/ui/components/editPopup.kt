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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MainTextBox

@Composable
fun EditPopup(
    modifier: Modifier = Modifier,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    isDialogVisible: Boolean
) {
    val focusManager = LocalFocusManager.current
    val (nickname, onNicknameChange) = remember { mutableStateOf("") }
    val (groupName, onGroupNameChange) = remember { mutableStateOf("") }
    val (name, onNameChange) = remember { mutableStateOf("") }

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
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
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

                    Text(
                        modifier = modifier.padding(PaddingValues(top = 15.dp)),
                        text = "수정하실 이름으로 바꿔주세요",
                        color = mainBlack,
                        style = Typography.bodyMedium
                    )

                    Column(
                        modifier = modifier
                            .padding(PaddingValues(top = 30.dp, bottom = 36.dp))
                            .wrapContentSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 첫 번째 Row - 닉네임
                        if (isDialogVisible) {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "닉네임",
                                    color = mainBlack,
                                    style = Typography.bodyMedium
                                )
                                Spacer(modifier = modifier.width(12.dp))
                                MainTextBox(
                                    modifier = modifier,
                                    focusManager = focusManager,
                                    inputText = nickname,
                                    onInputTextChange = onNicknameChange,
                                )
                            }

                        // 두 번째 Row - 그룹명 - isDialogVisible에 따라 표시 결정
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "그룹명",
                                    color = mainBlack,
                                    style = Typography.bodyMedium
                                )
                                Spacer(modifier = modifier.width(12.dp))
                                MainTextBox(
                                    modifier = modifier,
                                    focusManager = focusManager,
                                    inputText = groupName,
                                    onInputTextChange = onGroupNameChange,
                                )
                            }
                        } else {
                            MainTextBox(
                                modifier = modifier,
                                focusManager = focusManager,
                                inputText = name,
                                onInputTextChange = onNameChange,
                            )
                        }
                    }

                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
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
private fun EditPopupPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EditPopup(onConfirmation = {},
            onDismissRequest = {},
            isDialogVisible = false
        )
    }
}

