package com.example.mhnfe.ui.screens.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack


@Composable
fun QRGenerateScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: QRViewModel = viewModel(),
    userType: UserType = UserType.CCTV,
){
    val uiState by viewModel.uiState.collectAsState()

    // UserType 설정
    LaunchedEffect(userType) {
        viewModel.setUserType(userType)
    }

    //QR 이미지 생성
    val qrBitmap = remember(uiState.qrContent) {
        viewModel.generateQRBitmap(500)
    }
    Scaffold(
        topBar = {
            SubTopBar(
                text = uiState.title,
                onBack = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(120.dp, alignment = Alignment.CenterVertically)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    style = Typography.bodyMedium,
                    color = mainBlack,
                    text = uiState.message
                )
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = modifier
                        .size(200.dp)
                        .border(1.dp, mainBlack)
                )
            }
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun QRGeneratePreview(){
//    val navController = rememberNavController()
//    val viewModel: QRViewModel = viewModel()
//
//    QRGenerateScreen(viewModel = viewModel, navController = navController)
//
//}