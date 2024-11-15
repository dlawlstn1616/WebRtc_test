plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mhnfe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mhnfe"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    //webRTC 공식 지원 종료
    // 로컬 WebRTC.aar 파일 사용
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val nav_version = "2.8.0"

    //화면 이동 의존성 추가
    implementation("androidx.navigation:navigation-compose:$nav_version")
    //QR 생성, 리더기 의존성 추가
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    //Add CameraX dependency
    val cameraxVersion = "1.4.0-alpha02"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-video:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    val awsVersion = "2.77.0"
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-signaling:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-webrtcstorage:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-mobile-client:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-auth-userpools:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-auth-ui:${awsVersion}@aar") { isTransitive = true }

    implementation("org.awaitility:awaitility:4.2.0")
    implementation("org.json:json:20190722")
    implementation("com.google.guava:guava:28.1-android")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")


    //Add OkHttp dependency
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    //Add Awaitility dependency
    implementation("org.awaitility:awaitility:4.2.0")
}