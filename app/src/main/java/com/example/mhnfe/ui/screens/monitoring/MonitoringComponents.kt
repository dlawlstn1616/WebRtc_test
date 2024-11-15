package com.example.mhnfe.ui.screens.monitoring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mhnfe.R
import com.example.mhnfe.data.model.CCTV
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainGray3

@Composable
fun CCTVItemCard(
    cctv: CCTV,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = mainGray3
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cctv.deviceName,
                style = Typography.bodyMedium
            )
            Row(
                modifier = modifier
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = modifier.size(24.dp)
                ) {
                    Icon(
                        modifier = modifier.size(20.dp, 16.dp).align(Alignment.Center),
                        painter = painterResource(id = if(cctv.networkStatus == "양호") R.drawable.good_signal else R.drawable.bad_signal),
                        contentDescription = "signal",
                        tint = Color.Unspecified
                    )
                }
                Box(
                    modifier = modifier.size(24.dp)
                ) {
                    Icon(
                        modifier = modifier.size(20.dp, 16.dp).align(Alignment.Center),
                        painter = painterResource(
                            id = when {
                                //배터리 양 별로 다른 아이콘
                                cctv.batteryStatus >= 80 -> R.drawable.full_battery
                                cctv.batteryStatus >= 40 -> R.drawable.medium_battery
                                else -> R.drawable.low_battery
                            }),
                        contentDescription = "편집"
                    )
                }
                IconButton(
                    modifier = modifier.size(24.dp),
                    onClick = onEdit
                ) {
                    Icon(
                        modifier = modifier.size(6.dp, 20.dp),
                        painter = painterResource(id = R.drawable.cctv_setting),
                        contentDescription = "편집"
                    )
                }
            }

        }
    }
}