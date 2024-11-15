package com.example.mhnfe.ui.screens.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mhnfe.ui.screens.qr.QRViewModel

@Composable
fun MasterMainScreen(
    modifier: Modifier = Modifier,
    qrViewModel: QRViewModel = viewModel(),
    onQRButtonClick: () -> Unit,
    onCCTVButtonClick: () -> Unit,
    onViewerButtonClick:() -> Unit
){
    Column (
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically)
    ){
        Button(
            onClick = {
                qrViewModel.generateNewQRCode()
                onQRButtonClick()
            }
        ) {
            Text(text = "QR 생성")
        }

        Button(
            onClick = {
                onCCTVButtonClick()
            }
        ) {
            Text(text = "CCTV")
        }

        Button(
            onClick = {
                onViewerButtonClick()
            }
        ) {
            Text(text = "Viewer")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MasterPreview(){
    MasterMainScreen(onQRButtonClick = {}, onCCTVButtonClick = {}, onViewerButtonClick = {})

}