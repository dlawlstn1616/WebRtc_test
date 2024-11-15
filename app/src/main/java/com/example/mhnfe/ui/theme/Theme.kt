package com.example.mhnfe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    background = Color.White,
    onSurface = mainBlack, // 텍스트 컬러
)

private val LightColorScheme = lightColorScheme(
    background = Color.White,
    onSurface = mainBlack,
)

@Composable
fun MhnFETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = LightColorScheme  // 항상 라이트 모드 색상 사용

    MaterialTheme(
        colorScheme = colors,
        content = content,
        typography = typography,
    )
}