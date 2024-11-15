package com.example.mhnfe.data.model

import java.time.LocalDateTime

data class CCTV(
    val id: String,
    val deviceName: String,   // 기기명
    val model: String,        // 기종
    val os: String,           // OS
    val appVersion: String,   // 앱 버전
    val batteryStatus: Int, // 배터리 상태
    val networkStatus: String  // 네트워크 상태
)

//아무것도 없는거 테스트 할때
val emptyCCTVList = emptyList<CCTV>()

//CCTV 예시 데이터
val sampleCCTVList = listOf(
    CCTV(
        id = "1",
        deviceName = "주방",
        model = "Flip5",
        os = "Android 10",
        appVersion = "2.3.1",
        batteryStatus = 70,
        networkStatus = "양호"
    ),
    CCTV(
        id = "2",
        deviceName = "거실",
        model = "s24",
        os = "Android 10",
        appVersion = "3.0.0",
        batteryStatus = 30,
        networkStatus = "나쁨"
    ),
    CCTV(
        id = "3",
        deviceName = "방1",
        model = "s23",
        os = "Android 12",
        appVersion = "1.5.2",
        batteryStatus = 95,
        networkStatus = "나쁨"
    ),
)

data class ReportItem(
    val date: LocalDateTime, // 나중에 형식 바꾸기
)

val reportItems = listOf(
        ReportItem(LocalDateTime.of(2024, 10, 22, 13, 0)),
        ReportItem(LocalDateTime.of(2024, 10, 23, 14, 30)),
        ReportItem(LocalDateTime.of(2024, 10, 24, 15, 45))
    )