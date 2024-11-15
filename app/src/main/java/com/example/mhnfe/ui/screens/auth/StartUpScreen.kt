package com.example.mhnfe.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.navigation.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Cognito 로그인
@Composable
fun StartUpScreen(
    auth: AWSMobileClient,
    navController: NavController
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 코루틴 스코프 생성
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WebRTC Login",
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = null
            },
            label = { Text("이메일") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    isLoading = true
                    errorMessage = null

                    try {
                        withContext(Dispatchers.IO) {
                            Log.d("awskinesisvideo", "로그인 시도 시작 - username: $username")

                            val signInResult = AWSMobileClient.getInstance().signIn(username, password, null)
                            Log.d("awskinesisvideo", "로그인 응답: ${signInResult.signInState}")

                            if (signInResult.signInState == SignInState.NEW_PASSWORD_REQUIRED) {
                                // 강제로 새 비밀번호 설정
                                val parameters = hashMapOf<String, String>()
                                parameters["newPassword"] = "qqww1122"

                                try {
                                    AWSMobileClient.getInstance()
                                    Log.d("awskinesisvideo", "새 비밀번호 설정 성공")

                                    withContext(Dispatchers.Main) {
                                        navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                                            popUpTo(NavRoutes.Auth.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("awskinesisvideo", "새 비밀번호 설정 실패", e)
                                    withContext(Dispatchers.Main) {
                                        errorMessage = "비밀번호 변경 실패: ${e.message}"
                                    }
                                }
                            } else if (signInResult.signInState == SignInState.DONE) {
                                withContext(Dispatchers.Main) {
                                    //화면 이동
                                    navController.navigate(NavRoutes.Main.createRoute(UserType.MASTER)) {
                                        // Auth 플로우를 백스택에서 제거
                                        popUpTo(NavRoutes.Auth.route) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("awskinesisvideo", "로그인 실패", e)
                        withContext(Dispatchers.Main) {
                            errorMessage = when (e) {
                                is UserNotConfirmedException -> "이메일 인증이 필요합니다"
                                is NotAuthorizedException -> "아이디 또는 비밀번호가 올바르지 않습니다"
                                is UserNotFoundException -> "존재하지 않는 사용자입니다"
                                else -> "로그인 중 오류가 발생했습니다: ${e.message}"
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("로그인")
            }
        }
    }
}