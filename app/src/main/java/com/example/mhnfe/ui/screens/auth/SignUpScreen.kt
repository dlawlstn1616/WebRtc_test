package com.example.mhnfe.ui.screens.auth


import androidx.compose.material3.Text
import androidx.compose.runtime.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.MainTextBox
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainYellow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var currentStep by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    // State variables for input values
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    // Error states for each field
    var isEmailError by remember { mutableStateOf(false) }
    var isVerificationError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }
    var isNicknameError by remember { mutableStateOf(false) }

    // Error message states
    var emailErrorMessage by remember { mutableStateOf("") }
    var verificationErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var nicknameErrorMessage by remember { mutableStateOf("") }

    // Validation functions
    fun isEmailDuplicate(email: String): Boolean {
        return email == "test@naver.com"  // 테스트용 중복 이메일
    }
    fun isVerificationCodeValid(code: String): Boolean {
        return code != "111111"  // 111111이면 틀린 것으로 처리
    }

    fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            0 -> {
                when {
                    email.isEmpty() -> {
                        emailErrorMessage = "이메일을 입력해주세요."
                        isEmailError = true
                        false
                    }
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        emailErrorMessage = "올바른 이메일 형식이 아닙니다."
                        isEmailError = true
                        false
                    }
                    isEmailDuplicate(email) -> {
                        emailErrorMessage = "이미 사용 중인 이메일입니다."
                        isEmailError = true
                        false
                    }
                    else -> {
                        isEmailError = false
                        emailErrorMessage = ""
                        true
                    }
                }
            }
            1 -> {
                when {
                    verificationCode.isEmpty() -> {
                        verificationErrorMessage = "인증번호를 입력해주세요."
                        isVerificationError = true
                        false
                    }
                    !isVerificationCodeValid(verificationCode) -> {
                        verificationErrorMessage = "인증번호가 올바르지 않습니다."
                        isVerificationError = true
                        false
                    }
                    else -> {
                        isVerificationError = false
                        verificationErrorMessage = ""
                        true
                    }
                }
            }
            2 -> {
                when {
                    password.isEmpty() -> {
                        passwordErrorMessage = "비밀번호를 입력해주세요."
                        isPasswordError = true
                        false
                    }
                    password.length < 8 -> {
                        passwordErrorMessage = "비밀번호는 8자리 이상이어야 합니다."
                        isPasswordError = true
                        false
                    }
                    password != confirmPassword -> {
                        passwordErrorMessage = "비밀번호가 일치하지 않습니다."
                        isConfirmPasswordError = true
                        false
                    }
                    else -> {
                        isPasswordError = false
                        isConfirmPasswordError = false
                        passwordErrorMessage = ""
                        true
                    }
                }
            }
            3 -> {
                when {
                    nickname.isEmpty() -> {
                        nicknameErrorMessage = "닉네임을 입력해주세요."
                        isNicknameError = true
                        false
                    }
                    else -> {
                        isNicknameError = false
                        nicknameErrorMessage = ""
                        true
                    }
                }
            }
            else -> false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SubTopBar(
                text = "회원가입",
                onBack = {
                    if (currentStep > 0) {
                        currentStep--
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .background(color = Color.White)
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 34.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                when (currentStep) {
                    0 -> {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = mainYellow)) {
                                    append("아이디")
                                }
                                withStyle(SpanStyle(color = mainBlack)) {
                                    append("를 입력해주세요")
                                }
                            }
                        )
                        MainTextBox(
                            focusManager = focusManager,
                            inputText = email,
                            onInputTextChange = {
                                email = it
                                isEmailError = false
                                emailErrorMessage = ""
                            },
                            isError = isEmailError,
                            onIsErrorChange = { isEmailError = it },
                            hintText = "example@example.com",
                            warningText = emailErrorMessage
                        )
                    }
                    1 -> {
                        Text(text = "인증번호를 입력해주세요")
                        MainTextBox(
                            focusManager = focusManager,
                            inputText = verificationCode,
                            onInputTextChange = {
                                verificationCode = it
                                isVerificationError = false
                                verificationErrorMessage = ""
                            },
                            isError = isVerificationError,
                            onIsErrorChange = { isVerificationError = it },
                            hintText = "인증번호 6자리",
                            warningText = verificationErrorMessage
                        )
                    }
                    2 -> {
                        Text(text = "비밀번호를 입력해주세요")
                        MainTextBox(
                            focusManager = focusManager,
                            inputText = password,
                            onInputTextChange = {
                                password = it
                                isPasswordError = false
                                passwordErrorMessage = ""
                            },
                            isError = isPasswordError,
                            onIsErrorChange = { isPasswordError = it },
                            hintText = "8자리 이상 입력해주세요",
                            warningText = passwordErrorMessage
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "비밀번호 확인")
                        MainTextBox(
                            focusManager = focusManager,
                            inputText = confirmPassword,
                            onInputTextChange = {
                                confirmPassword = it
                                isConfirmPasswordError = false
                            },
                            isError = isConfirmPasswordError,
                            onIsErrorChange = { isConfirmPasswordError = it },
                            hintText = "비밀번호를 한번 더 입력해주세요",
                            warningText = if (isConfirmPasswordError) "비밀번호가 일치하지 않습니다." else ""
                        )
                    }
                    3 -> {
                        Text(text = "닉네임을 입력해주세요")
                        MainTextBox(
                            focusManager = focusManager,
                            inputText = nickname,
                            onInputTextChange = {
                                nickname = it
                                isNicknameError = false
                                nicknameErrorMessage = ""
                            },
                            isError = isNicknameError,
                            onIsErrorChange = { isNicknameError = it },
                            hintText = "닉네임을 입력해주세요",
                            warningText = nicknameErrorMessage
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(
                    id = when (currentStep) {
                        0 -> R.drawable.dog_1
                        1 -> R.drawable.dog_2
                        2 -> R.drawable.dog_3
                        else -> R.drawable.dog_4
                    }
                ),
                contentDescription = "단계별 이미지",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MiddleButton(
                    text = if (currentStep == 3) "완료" else "다음",
                    onClick = {
                        if (validateCurrentStep()) {
                            if (currentStep < 3) {
                                currentStep++
                            } else {
                                onLoginClick()
                            }
                        }
                    }
                )

                if (currentStep == 0) {
                    Text(
                        text = "아이디가 있으신가요?",
                        textAlign = TextAlign.Center,
                        color = mainGray
                    )
                }
            }
        }
    }
}

@Preview(
    name = "SignUp Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        navController = rememberNavController(),
        onLoginClick = {}
    )
}
