package com.example.mhnfe.ui.screens.monitoring

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.R
import com.example.mhnfe.data.model.sampleCCTVList
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray2

@Composable
fun DeviceInfoScreen(
    modifier: Modifier = Modifier,
    cctvId: String,
    navController: NavController,
) {
    val cctv = sampleCCTVList.find { it.id == cctvId }

    Scaffold(
        topBar = {
            SubTopBar(text = "기기 정보", onBack = { navController.popBackStack()})
        },
    ) { innerPadding ->
        if (cctv != null) {
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Column(
                    modifier = modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 45.dp, horizontal = 34.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(40.dp , alignment = Alignment.CenterVertically)
                ) {
                    DeviceInfoCard(title = "기기 정보", label1 = "기기명", label2 = "기종", value1 = cctv.deviceName, value2 = cctv.model)
                    DeviceInfoCard(title = "버전 관리", label1 = "OS", label2 = "앱 버전", value1 = cctv.os, value2 = cctv.appVersion)
                    DeviceInfoCard(title = "연결 상태", label1 = "배터리", label2 = "네트워크 상태", value1 = cctv.batteryStatus.toString(), value2 = cctv.networkStatus)
                    Box(
                        modifier = modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo2),
                            contentDescription = "로고",
                            modifier = modifier.size(147.dp, 168.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                }
            }
        } else {
            Column (
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    style = Typography.bodyMedium,
                    text = "CCTV 정보를 찾을 수 없습니다.",
                    color = mainBlack
                )
            }
        }
    }
}

//기기 정보 UI
@Composable
private fun DeviceInfoCard(
    modifier: Modifier = Modifier,
    title : String,
    label1: String, value1: String,
    label2: String, value2: String
){
    Column(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(title)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = mainGray2, shape = RoundedCornerShape(8.dp))
            ,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label1,
                    modifier = modifier.width(120.dp)
                )
                Text(
                    text = value1,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.Start)
                )
            }
            HorizontalDivider(color = Color.White, thickness = 1.dp)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label2,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    text = value2,
                    modifier = modifier
                        .wrapContentWidth(Alignment.Start)
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun GroupScreenPreview() {
    val navController = rememberNavController()
    val cctv = sampleCCTVList.first()
    DeviceInfoScreen(cctvId = cctv.id,navController = navController)
//    GroupScreen(
//        userType = UserType.VIEWER,
//        navController = navController
//    )
}