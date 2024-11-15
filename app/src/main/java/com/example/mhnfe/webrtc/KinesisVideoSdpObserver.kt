package com.example.mhnfe.webrtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * WebRTC의 SDP (Session Description Protocol) 관련 이벤트를 처리하는 옵저버 클래스입니다.
 * SDP는 피어 간의 미디어 세션 정보를 교환하는 데 사용됩니다.
 */
open class KinesisVideoSdpObserver : SdpObserver {

    companion object {
        private val TAG = KinesisVideoSdpObserver::class.java.simpleName
    }

    /**
     * SDP 생성이 성공했을 때 호출됩니다.
     * 로컬 또는 원격 세션 설명이 성공적으로 생성되었음을 나타냅니다.
     */
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Log.d(TAG, "onCreateSuccess(): SDP=${sessionDescription.description}")
    }

    /**
     * SDP 설정이 성공했을 때 호출됩니다.
     * 로컬 또는 원격 세션 설정이 성공적으로 적용되었음을 나타냅니다.
     */
    override fun onSetSuccess() {
        Log.d(TAG, "onSetSuccess(): SDP")
    }

    /**
     * SDP 생성이 실패했을 때 호출됩니다.
     * 세션 설명을 생성하는 과정에서 오류가 발생했음을 나타냅니다.
     */
    override fun onCreateFailure(error: String) {
        Log.e(TAG, "onCreateFailure(): Error=$error")
    }

    /**
     * SDP 설정이 실패했을 때 호출됩니다.
     * 세션 설정을 적용하는 과정에서 오류가 발생했음을 나타냅니다.
     */
    override fun onSetFailure(error: String) {
        Log.e(TAG, "onSetFailure(): Error=$error")
    }
}