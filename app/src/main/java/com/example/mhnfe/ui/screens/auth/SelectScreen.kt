package com.example.mhnfe.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.components.MiddleButton
import com.example.mhnfe.ui.theme.mainYellow
import com.example.mhnfe.ui.theme.mainGray
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign

// 그룹 생성용 버튼 컴포넌트
@Composable
fun GroupCreateButton(
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(vertical = 15.dp, horizontal = 140.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = mainGray
        ),
        onClick = onClick,
    ) {
        Text(
            text = "그룹 생성",
        )
    }
}

@Composable
fun SelectScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onQrScanClick: () -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier = modifier,
        topBar = {
            SubTopBar(
                text = "",
                onBack = { navController.popBackStack() }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 로고 이미지
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "로고",
                modifier = Modifier
                    .padding(top = 80.dp)
                    .size(300.dp),
                contentScale = ContentScale.Fit
            )

            // 버튼들
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                // QR 스캔 버튼
                MiddleButton(
                    text = "참여 QR",
                    onClick = onQrScanClick
                )

                // 그룹 생성 버튼
                GroupCreateButton(
                    onClick = onCreateGroupClick
                )
            }
        }
    }
}

@Preview(
    name = "Select Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun SelectScreenPreview() {
    SelectScreen(
        navController = rememberNavController(),
        onQrScanClick = {},
        onCreateGroupClick = {}
    )
}
