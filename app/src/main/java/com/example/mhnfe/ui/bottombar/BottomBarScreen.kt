package com.example.mhnfe.ui.bottombar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mhnfe.R
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.hoverYellow
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainYellow

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    data object Monitoring : NavigationItem("monitoring", R.drawable.monitoring, "모니터링")
    data object Report : NavigationItem("report", R.drawable.report, "리포트")
    data object MyPage : NavigationItem("myPage", R.drawable.mypage, "마이페이지")
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(hoverYellow),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            NavigationItem.Monitoring,
            NavigationItem.Report,
            NavigationItem.MyPage,
        )

        val currentRoute = navBackStackEntry?.destination?.route
        Log.d("Navigation", "Current Route: $currentRoute")

        items.forEach { item ->
            val isSelected = when (item) {
                is NavigationItem.Monitoring -> {
                    currentRoute == "monitoring/group"
                }

                is NavigationItem.Report -> {
                    currentRoute == "report/report_detail"
                }

                is NavigationItem.MyPage -> {
                    currentRoute == "myPage/profile"
                }
            }

            // isSelected 상태 디버깅
            Log.d("Navigation", "Item: ${item.title}, IsSelected: $isSelected")

            Box(
                modifier = modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) mainYellow else hoverYellow
                    )
                    .noRippleClickable {
                        // 네비게이션 로직 수정
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.Monitoring.Group.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = modifier.wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        modifier = modifier.size(30.dp),
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        tint = mainBlack
                    )
                    Text(
                        text = item.title,
                        style = Typography.labelSmall,
                        color = mainBlack
                    )
                }
            }
        }
    }
}

// 리플 효과 없는 클릭 modifier
fun Modifier.noRippleClickable(
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        onClick()
    }
}

