package com.example.mhnfe.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.data.model.reportItems
import com.example.mhnfe.ui.components.MainTopBar
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun ReportDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainTopBar(text = "리포트")
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(vertical = 28.dp)
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Calendar(
                modifier = modifier
                    .padding(horizontal = 22.dp),
                initialDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                    // 여기서 선택된 날짜 처리 영상이랑 연결해야함
                }
            )
            Column (
                modifier = modifier.fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(25.dp, alignment = Alignment.CenterVertically)
            ) {
                Text(
                    modifier = modifier
                        .padding(horizontal = 22.dp),
                    text = "🎥영상",
                    style = Typography.labelLarge,
                    color = mainBlack
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(
                        8.dp,
                        alignment = Alignment.Start
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 22.dp),
                ){
                    items(
                        items = reportItems,
                    ) { reportItem ->
                        ReportItemCard(
                            onClick = {},
                            date = reportItem.date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm"))
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview(){
    val navController = rememberNavController()
    ReportDetailScreen(navController = navController)

}