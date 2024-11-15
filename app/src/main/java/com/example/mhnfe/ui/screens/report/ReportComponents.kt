package com.example.mhnfe.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mhnfe.R
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainYellow
import java.time.LocalDate

@Composable
fun ReportItemCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    date: String
) {
    Card(
        modifier = modifier
            .size(340.dp,200.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = mainBlack
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                modifier = modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                text = date,
                textAlign = TextAlign.End,
                color = Color.White,
                style = Typography.labelSmall,

            )
        }
    }
}

@Composable
fun Calendar(
    modifier: Modifier= Modifier,
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit = {}
) {
    var currentDate by remember { mutableStateOf(initialDate) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // Calendar Header with Month and Year
        CalendarHeader(
            currentDate = currentDate,
            onPreviousMonth = {
                currentDate = currentDate.minusMonths(1)
            },
            onNextMonth = {
                currentDate = currentDate.plusMonths(1)
            }
        )

        // Week days header
        WeekDayHeader()

        // Calendar days grid
        CalendarGrid(
            currentDate = currentDate,
            onDateSelected = { selectedDate ->
                currentDate = selectedDate
                onDateSelected(selectedDate)
            }
        )
    }
}

@Composable
private fun CalendarHeader(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = modifier.size(50.dp),
            onClick = onPreviousMonth
        ) {
            Icon(
                modifier = modifier.size(15.dp, 25.dp),
                painter = painterResource(id = R.drawable.navigate_before),
                tint = mainBlack,
                contentDescription = "이전"
            )
        }

        Text(
            style = Typography.titleMedium,
            color = mainBlack,
            text = "${currentDate.year}년 ${currentDate.monthValue}월",
        )

        IconButton(
            modifier = modifier.size(50.dp),
            onClick = onNextMonth
        ) {
            Icon(
                modifier = modifier.size(15.dp, 25.dp),
                painter = painterResource(id = R.drawable.navigate_after),
                tint = mainBlack,
                contentDescription = "다음"
            )
        }
    }
}

@Composable
private fun WeekDayHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 23.dp).fillMaxWidth().wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
        weekDays.forEach { day ->
            Text(
                style = Typography.labelMedium,
                text = day,
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = when (day) {
                    "일" -> Color.Red
                    "토" -> Color.Blue
                    else -> mainGray
                }
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentDate.withDayOfMonth(1)
    val lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth())
    val firstDayWeekday = firstDayOfMonth.dayOfWeek.value % 7

    val days = mutableListOf<LocalDate?>()

    // Add empty spaces for days before the first day of month
    repeat(firstDayWeekday) { days.add(null) }

    // Add all days of the month
    for (i in 1..lastDayOfMonth.dayOfMonth) {
        days.add(currentDate.withDayOfMonth(i))
    }

    // Add empty spaces for remaining days to complete the grid
    val remainingDays = (7 - (days.size % 7)) % 7
    repeat(remainingDays) { days.add(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically),
        userScrollEnabled = false
    ) {
        items(days) { date ->
            CalendarDay(
                date = date,
                isSelected = date?.equals(currentDate) == true,
                isToday = date?.equals(LocalDate.now()) == true,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onDateSelected: (LocalDate) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .then(
                if (date != null) {
                    Modifier.clickable { onDateSelected(date) }
                } else Modifier
            )
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = mainYellow,
                        shape = CircleShape
                    )
                } else if (isToday) {
                    Modifier.border(
                        width = 1.dp,
                        color = mainYellow,
                        shape = CircleShape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    date.dayOfWeek.value == 7 -> Color.Red  // Sunday
                    date.dayOfWeek.value == 6 -> Color.Blue // Saturday
                    else -> mainBlack
                },
                style = Typography.labelMedium
            )
        }
    }
}