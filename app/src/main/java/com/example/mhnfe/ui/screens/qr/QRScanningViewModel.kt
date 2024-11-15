package com.example.mhnfe.ui.screens.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QRScanningViewModel : ViewModel() {
    private var _qrCodeResult: String? = null
    val qrCodeResult: String?
        get() = _qrCodeResult

    private var _showMessage: String? = null
    val showMessage: String?
        get() = _showMessage

    fun onQRCodeScanned(result: String) {
        // QR 코드가 스캔되었을 때 호출되는 메소드
        _qrCodeResult = result
        _showMessage = "QR 코드가 인식되었습니다: $result" // 메시지 설정

        // 필요한 추가 작업 수행
    }

    fun resetQRCodeResult() {
        // QR 코드 결과를 초기화하는 메소드
        _qrCodeResult = null
        _showMessage = null // 메시지 초기화
    }
}