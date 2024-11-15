package com.example.mhnfe.ui.screens.qr

import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import com.example.mhnfe.di.UserType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class QRScreenUiState(
    val title: String = "",
    val message: String = "",
    val qrContent: String = UUID.randomUUID().toString(),
    val qrBitmap: ImageBitmap? = null,
    val userType: UserType = UserType.CCTV
)

class QRViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(QRScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 초기 QR 코드 생성
        generateNewQRCode()
    }


    fun setUserType(type: UserType) {
        _uiState.update { currentState ->
            currentState.copy(
                userType = type,
                title = when (type) {
                    UserType.CCTV -> "CCTV 등록"
                    UserType.VIEWER -> "뷰어 등록"
                    UserType.MASTER -> ""
                },
                message = when (type) {
                    UserType.CCTV -> "CCTV로 사용할 기기에서\nQR 인증을 해주세요"
                    UserType.VIEWER -> "뷰어로 사용할 기기에서\nQR 인증을 해주세요"
                    UserType.MASTER -> ""
                }
            )
        }
    }

    // QR 코드에 들어갈 내용을 생성
    fun generateNewQRCode() {
        _uiState.update { currentState ->
            currentState.copy(
                qrContent = UUID.randomUUID().toString()
            )
        }
    }


    // QR Content를 QR 코드 이미지로 변환
    fun generateQRBitmap(size: Int): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(
                uiState.value.qrContent,  // uiState에서 qrContent 가져오기
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            canvas.drawColor(android.graphics.Color.WHITE)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
            }

            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) {
                        canvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            (x + 1).toFloat(),
                            (y + 1).toFloat(),
                            paint
                        )
                    }
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}