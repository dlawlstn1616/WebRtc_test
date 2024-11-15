package com.example.mhnfe.ui.screens.monitoring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.data.model.emptyCCTVList
import com.example.mhnfe.data.model.sampleCCTVList
import com.example.mhnfe.di.UserType
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.components.SmallButton
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack


@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    groupId: String = "그룹1",
    userType: UserType = UserType.MASTER,
    //나중에 뷰모델로 뺄 것
    cctv: List<CCTV> = sampleCCTVList,
    navController: NavController
    ) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(text = groupId)
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(vertical = 28.dp, horizontal = 34.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp, alignment = Alignment.CenterVertically)
        ) {
            //마스터 화면 일 때 버튼 추가
            if (userType == UserType.MASTER) {
                Row(
                    modifier = modifier.fillMaxWidth().wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.CCTV)
                            )
                        },
                        text = "CCTV 추가"
                    )
                    SmallButton(
                        onClick = {
                            navController.navigate(
                                NavRoutes.Monitoring.QRGenerate.createRoute(UserType.VIEWER)
                            )
                        },
                        text = "참여자 추가"
                    )
                }
            }
            if (cctv.isEmpty()) {
                Column (
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        style = Typography.bodyMedium,
                        text = "등록된 CCTV가 없습니다.",
                        color = mainBlack
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(
                        items = cctv,
                        key = { it.id }
                    ) { cctvItem ->
                        CCTVItemCard(
                            cctv = cctvItem,
                            onClick = {
                                // cctv 화면으로 이동
                                // navController.navigate("camera/${cctvItem.id}")
                            },
                            onEdit = {
                                navController.navigate(NavRoutes.Monitoring.DeviceInformation.createRoute(cctvItem.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun GroupScreenPreview() {
//    val navController = rememberNavController()
//    GroupScreen(
//        userType = UserType.VIEWER,
//        navController = navController,
//        cctv = sampleCCTVList
//    )
//}
//@Preview(showBackground = true)
//@Composable
//private fun GroupScreenPreview2() {
//    val navController = rememberNavController()
//    GroupScreen(
//        userType = UserType.VIEWER,
//        navController = navController,
//        cctv = emptyCCTVList
//    )
//}
//@Preview(showBackground = true)
//@Composable
//private fun GroupScreenPreview3() {
//    val navController = rememberNavController()
//    GroupScreen(
//        userType = UserType.MASTER,
//        navController = navController,
//        cctv = sampleCCTVList
//    )
//}