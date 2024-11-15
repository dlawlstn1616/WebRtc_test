package com.example.mhnfe
import android.app.Application
import android.util.Log
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import org.json.JSONException

/**
 * 애플리케이션의 메인 Application 클래스
 * AWS 인증 정보와 리전 설정을 관리합니다.
 */
class MyApplication : Application() {
    companion object {
        private val TAG = MyApplication::class.java.simpleName

        /**
         * AWS 자격 증명 제공자를 반환합니다.
         * AWS 서비스 접근에 필요한 인증 정보를 제공합니다.
         */
        fun getCredentialsProvider(): AWSCredentialsProvider =
            AWSMobileClient.getInstance()

        /**
         *  파일을 파싱하여 AWS 리전을 추출합니다.
         *
         * @return AWS 리전 문자열, 설정이 없는 경우 null
         * @throws IllegalStateException awsconfiguration.json 파일이 올바르게 설정되지 않은 경우
         */
        fun getRegion(): String {
            val configuration = AWSMobileClient.getInstance().configuration
                ?: throw IllegalStateException("awsconfiguration.json이 올바르게 설정되지 않았습니다!")

            return try {
                configuration.optJsonObject("CredentialsProvider")
                    ?.getJSONObject("CognitoIdentity")
                    ?.getJSONObject("Default")
                    ?.getString("Region")
                    ?: throw IllegalStateException("리전 정보를 찾을 수 없습니다!")
            } catch (e: JSONException) {
                Log.e(TAG, "Cognito 설정에서 리전 정보 추출 중 오류 발생", e)
                throw IllegalStateException("리전 정보 파싱 실패", e)
            }
        }
    }
}