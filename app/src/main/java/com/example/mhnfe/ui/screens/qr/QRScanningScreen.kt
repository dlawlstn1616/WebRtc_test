package com.example.mhnfe.ui.screens.qr

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mhnfe.ui.components.SubTopBar
import com.example.mhnfe.ui.theme.Typography
import com.example.mhnfe.ui.theme.mainBlack
import com.example.mhnfe.ui.theme.mainGray
import com.example.mhnfe.ui.theme.mainGray2
import com.example.mhnfe.ui.theme.mainRed
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QRScanningScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: QRScanningViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanner = MultiFormatReader().apply {
        val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
        setHints(hints)
    }

    var isScanning by remember { mutableStateOf(true) } // 스캔 상태 변수 추가
    Scaffold(
        topBar = {
            SubTopBar(
                text = "QR 촬영",
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    ) {innerPadding ->
        BoxWithConstraints(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val screenWidth = maxWidth
            val scanBoxSize = screenWidth * 0.6f // adjust the size of the scanning area here

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CameraPreview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalysis = ImageAnalysis.Builder().build().also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                if (isScanning) { // 스캔이 가능할 때만 실행
                                    val result = scanQRCode(imageProxy, scanner)
                                    imageProxy.close()
                                    if (result != null) {
                                        println("QR Code found: ${result.text}")
                                        // QR 코드 인식 성공 시 ViewModel 호출
                                        viewModel.onQRCodeScanned(result.text)
                                        // 화면 전환
                                        //navController.navigate("")
                                        isScanning = false // 스캔 중지
                                    } else {
                                        imageProxy.close()
                                    }
                                } else {
                                    imageProxy.close() // 스캔이 멈췄을 경우 이미지 프록시 닫기
                                }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                context as ComponentActivity,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            // Handle exceptions
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = modifier.fillMaxSize()
            )

            Text(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 200.dp) // 상단에 여백을 주어 텍스트가 화면 위에 표시되도록
                    .zIndex(1f), // 텍스트가 다른 요소들 위로 오도록 설정
                text = "기기에서 QR 촬영을 완료해 주세요.",
                color = mainBlack,
                style = Typography.bodyMedium,
            )

            Box(
                modifier = modifier
                    .matchParentSize()
                    .background(mainGray2.copy(alpha = 0.6f))
            )

            // Scanning area box with a clear cutout
            Box(
                modifier = modifier
                    .size(scanBoxSize)
                    .align(Alignment.Center)
                    .drawBehind {
                        // Draw a rounded clear rectangle to create a cutout effect
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                    }
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(12.dp))
            )
        }
    }
}

private fun scanQRCode(imageProxy: ImageProxy, scanner: MultiFormatReader): Result? {
    val data = imageProxy.planes[0].buffer.let { buffer ->
        val data = ByteArray(buffer.capacity())
        buffer.get(data)
        buffer.clear()
        data
    }
    val source = PlanarYUVLuminanceSource(
        data,
        imageProxy.width,
        imageProxy.height,
        0,
        0,
        imageProxy.width,
        imageProxy.height,
        false
    )
    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
    return try {
        scanner.decodeWithState(binaryBitmap)
    } catch (e: Exception) {
        null // QR Code not found
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun QRScanningScreenPreview() {
//    val navController = rememberNavController() // Navigation Controller 미리보기용
//    val viewModel = remember { QRScanningViewModel() } // ViewModel 미리보기용
//
//    QRScanningScreen(
//        modifier = Modifier.fillMaxSize(),
//        navController = navController,
//        viewModel = viewModel
//    )
//}