package com.example.mhnfe.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow
import com.example.mhnfe.ui.theme.mainGray3

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isAutoLogin by remember { mutableStateOf(false) }

    // 로그인 입력값 상태 관리
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 에러 상태 관리
    var isIdError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            SubTopBar(
                text = "로그인",
                onBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(110.dp, alignment = Alignment.Top)
        ) {
            // 상단부 (로고)
            Column(
                modifier = modifier.padding(vertical = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically)
            ) {
                // 로고 이미지
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "로고",
                    modifier = Modifier.size(250.dp),
                    contentScale = ContentScale.Fit
                )

                // 입력 필드들과 자동로그인을 포함하는 Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start
                ) {
                    MainTextBox(
                        focusManager = focusManager,
                        inputText = id,
                        onInputTextChange = { id = it },
                        isError = isIdError,
                        onIsErrorChange = { isIdError = it },
                        hintText = "아이디"
                    )

                    MainTextBox(
                        focusManager = focusManager,
                        inputText = password,
                        onInputTextChange = { password = it },
                        isError = isPasswordError,
                        onIsErrorChange = { isPasswordError = it },
                        hintText = "비밀번호"
                    )

                    // 자동 로그인 체크박스
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset((-12).dp)
                    ) {
                        Checkbox(
                            checked = isAutoLogin,
                            onCheckedChange = { isAutoLogin = it },
                            colors = CheckboxDefaults.colors(checkedColor = mainYellow)
                        )
                        Text(
                            text = "자동로그인",
                            color = mainGray
                        )
                    }
                }
            }

            // 하단부 버튼과 텍스트를 포함하는 Column
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 로그인 버튼
                MiddleButton(
                    text = "로그인",
                    onClick = {
                        // 로그인 validation 로직 추가 가능
                        onLoginClick()
                    }
                )

                // 비밀번호 찾기 텍스트
                Text(
                    text = "비밀번호를 잊어버리셨나요?",
                    textAlign = TextAlign.Center,
                    color = mainGray,
                    modifier = Modifier
                        .clickable { navController.navigate("forgot_password") }
                )
            }
        }
    }
}


//@Preview(
//    name = "Login Screen",
//    showBackground = true,
//    showSystemUi = true,
//    device = "spec:width=411dp,height=891dp"
//)
//@Composable
//fun LoginScreenPreview() {
//    LoginScreen(
//        navController = rememberNavController(),
//        onLoginClick = {}
//    )
//}

