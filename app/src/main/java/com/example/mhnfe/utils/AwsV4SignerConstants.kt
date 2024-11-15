package com.example.mhnfe.utils

/**
 * AWS V4 서명에 필요한 상수들을 정의하는 객체
 */
object AwsV4SignerConstants {
    const val ALGORITHM_AWS4_HMAC_SHA_256 = "AWS4-HMAC-SHA256"
    const val AWS4_REQUEST_TYPE = "aws4_request"
    const val SERVICE = "kinesisvideo"

    // AWS 표준 헤더 상수
    const val X_AMZ_ALGORITHM = "X-Amz-Algorithm"
    const val X_AMZ_CREDENTIAL = "X-Amz-Credential"
    const val X_AMZ_DATE = "X-Amz-Date"
    const val X_AMZ_EXPIRES = "X-Amz-Expires"
    const val X_AMZ_SECURITY_TOKEN = "X-Amz-Security-Token"
    const val X_AMZ_SIGNATURE = "X-Amz-Signature"
    const val X_AMZ_SIGNED_HEADERS = "X-Amz-SignedHeaders"

    // 형식 관련 상수
    const val NEW_LINE_DELIMITER = "\n"
    const val DATE_PATTERN = "yyyyMMdd"
    const val TIME_PATTERN = "yyyyMMdd'T'HHmmss'Z'"

    // HTTP 관련 상수
    const val METHOD = "GET"
    const val SIGNED_HEADERS = "host"
}