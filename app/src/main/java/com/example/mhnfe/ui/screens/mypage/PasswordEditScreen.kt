package com.example.mhnfe.ui.screens.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.hoverYellow
import com.example.mhnfe.ui.theme.mainBlack

@Composable
fun PasswordEditScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onClick: () -> Unit
) {
    var textConfig by rememberSaveable { mutableStateOf("") }
    var textPW by rememberSaveable { mutableStateOf("") }
    var textPWComfirm by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            SubTopBar(
                text = "비밀번호 변경 페이지",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    ) {innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 34.dp, vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(45.dp, alignment = Alignment.Top)
            ) {
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Top)
                ) {
                    MainTextBox(
                        focusManager = focusManager,
                        inputText = textConfig,
                        onInputTextChange = { newText ->
                            textConfig = newText
                        },
                        hintText = "인증코드"
                    )
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.spacedBy(
                            5.dp,
                            alignment = Alignment.End
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            modifier = modifier
                                .defaultMinSize(50.dp, 30.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = hoverYellow
                            ),
                            onClick = onClick,
                            border = BorderStroke(1.dp, mainBlack)
                        ) {
                            Text(
                                style = Typography.labelLarge,
                                text = "요청",
                                color = mainBlack
                            )
                        }
                        Button(
                            modifier = modifier
                                .defaultMinSize(50.dp, 30.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = hoverYellow
                            ),
                            onClick = onClick,
                            border = BorderStroke(1.dp, mainBlack)
                        ) {
                            Text(
                                style = Typography.labelLarge,
                                text = "인증",
                                color = mainBlack
                            )
                        }
                    }
                }
                MainTextBox(
                    focusManager = focusManager,
                    hintText = "새 비밀번호",
                    inputText = textPW,
                    onInputTextChange = { newText ->
                        textPW = newText
                    }
                )
                MainTextBox(
                    focusManager = focusManager,
                    hintText = "새 비밀번호 확인",
                    inputText = textPWComfirm,
                    onInputTextChange = { newText ->
                        textPWComfirm = newText
                    }
                )
            }
            MiddleButton(
                text = "확인"
            ) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PasswordEditPreview(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()
    PasswordEditScreen(
        navController = navController,
        onClick = {}
    )
}