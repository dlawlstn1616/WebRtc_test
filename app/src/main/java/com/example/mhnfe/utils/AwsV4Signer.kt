package com.example.mhnfe.utils

import com.amazonaws.util.BinaryUtils
import com.amazonaws.util.DateUtils
import com.google.common.hash.Hashing.sha256
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

@Suppress("SpellCheckingInspection", "SameParameterValue")
class AwsV4Signer {
    companion object {
        /**
         * Kinesis Video Signaling에 연결하기 위한 WebRTC WebSocket 연결 URI를 생성하고 서명합니다.
         *
         * @param uri URI to sign.
         *            Master 연결 URL - GetSignalingChannelEndpoint (master role) + Query Parameters: Channel ARN as X-Amz-ChannelARN
         *            Viewer 연결 URL - GetSignalingChannelEndpoint (viewer role) + Query Parameters: Channel ARN as X-Amz-ChannelARN & Client Id as X-Amz-ClientId
         * @param accessKey AWS Access Key Id
         * @param secretKey AWS Secret Key
         * @param sessionToken AWS Session Token (임시 자격 증명 사용시)
         * @param wssUri 쿼리 파라미터를 제외한 서명할 URL
         * @param region AWS 리전 (예: us-west-2)
         * @param dateMilli 요청 서명 시각 (epoch milliseconds)
         * @return Kinesis Video Signaling에 연결할 수 있는 서명된 WebSocket URL
         */
        fun sign(
            uri: URI,
            accessKey: String,
            secretKey: String,
            sessionToken: String?,
            wssUri: URI,
            region: String,
            dateMilli: Long
        ): URI {
            val amzDate = getTimeStamp(dateMilli)
            val datestamp = getDateStamp(dateMilli)
            val queryParamsMap = buildQueryParamsMap(uri, accessKey, sessionToken, region, amzDate, datestamp)
            val canonicalQuerystring = getCanonicalizedQueryString(queryParamsMap)
            val canonicalRequest = getCanonicalRequest(uri, canonicalQuerystring)
            val stringToSign = signString(amzDate, createCredentialScope(region, datestamp), canonicalRequest)
            val signatureKey = getSignatureKey(secretKey, datestamp, region, AwsV4SignerConstants.SERVICE)
            val signature = BinaryUtils.toHex(hmacSha256(stringToSign, signatureKey))
            val signedCanonicalQueryString = "$canonicalQuerystring&${AwsV4SignerConstants.X_AMZ_SIGNATURE}=$signature"

            return URI.create("${wssUri.scheme}://${wssUri.host}/?${getCanonicalUri(uri).substring(1)}$signedCanonicalQueryString")
        }

        fun sign(
            uri: URI,
            accessKey: String,
            secretKey: String,
            sessionToken: String?,
            region: String,
            dateMillis: Long
        ): URI {
            val wssUri = URI.create("wss://${uri.host}")
            return sign(uri, accessKey, secretKey, sessionToken, wssUri, region, dateMillis)
        }

        fun buildQueryParamsMap(
            uri: URI,
            accessKey: String,
            sessionToken: String?,
            region: String,
            amzDate: String,
            datestamp: String
        ): Map<String, String> {
            val queryParamsBuilder = mutableMapOf<String, String>()

            queryParamsBuilder.apply {
                put(AwsV4SignerConstants.X_AMZ_ALGORITHM, AwsV4SignerConstants.ALGORITHM_AWS4_HMAC_SHA_256)
                put(AwsV4SignerConstants.X_AMZ_CREDENTIAL, urlEncode("$accessKey/${createCredentialScope(region, datestamp)}"))
                put(AwsV4SignerConstants.X_AMZ_DATE, amzDate)
                put(AwsV4SignerConstants.X_AMZ_EXPIRES, "299")
                put(AwsV4SignerConstants.X_AMZ_SIGNED_HEADERS, AwsV4SignerConstants.SIGNED_HEADERS)
            }

            sessionToken?.takeIf { it.isNotEmpty() }?.let {
                queryParamsBuilder[AwsV4SignerConstants.X_AMZ_SECURITY_TOKEN] = urlEncode(it)
            }

            uri.query?.takeIf { it.isNotEmpty() }?.let { query ->
                query.split("&").forEach { param ->
                    param.split("=", limit = 2).let { parts ->
                        if (parts.size == 2) {
                            queryParamsBuilder[parts[0]] = urlEncode(parts[1])
                        }
                    }
                }
            }

            return queryParamsBuilder.toMap()
        }

        fun getCanonicalizedQueryString(queryParamsMap: Map<String, String>): String =
            queryParamsMap.entries
                .sortedBy { it.key }
                .joinToString("&") { "${it.key}=${it.value}" }

        fun createCredentialScope(region: String, datestamp: String): String =
            listOf(datestamp, region, AwsV4SignerConstants.SERVICE, AwsV4SignerConstants.AWS4_REQUEST_TYPE)
                .joinToString("/")

        fun getCanonicalRequest(uri: URI, canonicalQuerystring: String): String {
            val payloadHash = sha256().hashString("", UTF_8).toString()
            val canonicalUri = getCanonicalUri(uri)
            val canonicalHeaders = "host:${uri.host}${AwsV4SignerConstants.NEW_LINE_DELIMITER}"

            return listOf(
                AwsV4SignerConstants.METHOD,
                canonicalUri,
                canonicalQuerystring,
                canonicalHeaders,
                AwsV4SignerConstants.SIGNED_HEADERS,
                payloadHash
            ).joinToString(AwsV4SignerConstants.NEW_LINE_DELIMITER)
        }

        fun getCanonicalUri(uri: URI): String =
            uri.path.takeIf { it.isNotEmpty() } ?: "/"

        fun signString(amzDate: String, credentialScope: String, canonicalRequest: String): String =
            listOf(
                AwsV4SignerConstants.ALGORITHM_AWS4_HMAC_SHA_256,
                amzDate,
                credentialScope,
                sha256().hashString(canonicalRequest, UTF_8).toString()
            ).joinToString(AwsV4SignerConstants.NEW_LINE_DELIMITER)

        fun urlEncode(str: String): String = try {
            URLEncoder.encode(str, UTF_8.name())
        } catch (e: Exception) {
            throw IllegalArgumentException("URL 인코딩 실패: ${e.message}", e)
        }

        fun hmacSha256(data: String, key: ByteArray): ByteArray = try {
            Mac.getInstance("HmacSHA256").apply {
                init(SecretKeySpec(key, "HmacSHA256"))
            }.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            throw IllegalArgumentException("HMAC-SHA256 해시 생성 실패: ${e.message}", e)
        }

        fun getSignatureKey(
            key: String,
            dateStamp: String,
            regionName: String,
            serviceName: String
        ): ByteArray {
            val kSecret = "AWS4$key".toByteArray(StandardCharsets.UTF_8)
            val kDate = hmacSha256(dateStamp, kSecret)
            val kRegion = hmacSha256(regionName, kDate)
            val kService = hmacSha256(serviceName, kRegion)
            return hmacSha256(AwsV4SignerConstants.AWS4_REQUEST_TYPE, kService)
        }

        fun getTimeStamp(dateMilli: Long): String =
            DateUtils.format(AwsV4SignerConstants.TIME_PATTERN, Date(dateMilli))

        fun getDateStamp(dateMilli: Long): String =
            DateUtils.format(AwsV4SignerConstants.DATE_PATTERN, Date(dateMilli))
    }
}