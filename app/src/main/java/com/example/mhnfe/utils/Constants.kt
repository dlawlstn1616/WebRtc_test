package com.example.mhnfe.utils

import com.amazonaws.mobile.auth.core.BuildConfig

/**
 * AWS KVS WebRTC Android 클라이언트를 위한 상수 정의
 */
object Constants {
    /**
     * SDK 식별자
     */
    const val APP_NAME = "aws-kvs-webrtc-android-client"

    /**
     * SDK 버전 식별자
     */
    const val VERSION = BuildConfig.VERSION_NAME

    /**
     * Channel ARN을 위한 쿼리 파라미터.
     * Kinesis Video Websocket API 호출시 사용됩니다.
     */
    const val CHANNEL_ARN_QUERY_PARAM = "X-Amz-ChannelARN"

    /**
     * Client Id를 위한 쿼리 파라미터.
     * 뷰어에서만 사용되며, Kinesis Video Websocket API 호출시 사용됩니다.
     */
    const val CLIENT_ID_QUERY_PARAM = "X-Amz-ClientId"
}