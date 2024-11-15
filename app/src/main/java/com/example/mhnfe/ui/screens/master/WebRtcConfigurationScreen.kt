package com.example.mhnfe.ui.screens.master


import android.app.NotificationManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamWebRtcConfigurationScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    var channelName by remember { mutableStateOf("demo-channel") }
    var selectedCamera by remember { mutableStateOf(0) }  // 0: Front Camera, 1: Back Camera
    val cameraOptions = listOf("Front Camera", "Back Camera")
    var expanded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Channel Name Input
        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it },
            label = { Text("Channel Name") },
            modifier = Modifier.fillMaxWidth()
        )
        // Camera Selector
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = cameraOptions[selectedCamera],
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Camera") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                cameraOptions.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedCamera = index
                            expanded = false
                        }
                    )
                }
            }
        }

        // Master Button
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Master(CCTV)")
        }

        // Viewer Button
        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Viewer")
        }

    }
}
