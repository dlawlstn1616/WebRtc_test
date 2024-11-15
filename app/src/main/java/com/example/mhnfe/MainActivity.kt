package com.example.mhnfe

import androidx.compose.ui.Modifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mhnfe.ui.theme.MhnFETheme
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory
import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.example.mhnfe.ui.navigation.AppNavigation
import com.example.mhnfe.ui.navigation.NavRoutes
import com.example.mhnfe.ui.screens.master.KVSSignalingViewModel
import com.example.mhnfe.ui.screens.master.WebRTCUiState
import com.example.mhnfe.ui.screens.master.WebRtcConfig
import com.example.mhnfe.utils.PermissionManager
import kotlinx.coroutines.launch
import org.webrtc.RendererCommon
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CountDownLatch

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        val auth = AWSMobileClient.getInstance()
        initializeMobileClient(auth, this@MainActivity)
        //로그아웃
//        AWSMobileClient.getInstance().signOut()
        //권한 요청
        permissionManager.checkAndRequestPermissions()

        setContent {
            MhnFETheme {
                AppNavigation()

            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (::permissionManager.isInitialized) {
            permissionManager.checkAndRequestPermissions()
        }
    }
}

//Cognito 인증 초기화 (json 파일 사용)
private fun initializeMobileClient(client: AWSMobileClient, context: ComponentActivity) {
    val latch = CountDownLatch(1)
    client.initialize(context, object : Callback<UserStateDetails> {
        override fun onResult(result: UserStateDetails) {
            Log.d(
                "awskinesisvideo",
                "onResult: user state: " + result.userState
            )
            latch.countDown()
        }

        override fun onError(e: Exception) {
            Log.e(
                "awskinesisvideo",
                "onError: Initialization error of the mobile client",
                e
            )
            latch.countDown()
        }
    })
    try {
        latch.await()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

//class WebRtcViewModelFactory(
//    private val context: Context,
//    private val notificationManager: NotificationManager,
//    private val kvsSignalingViewModel: KVSSignalingViewModel
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(WebRtcViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return WebRtcViewModel(kvsSignalingViewModel, context, notificationManager) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//private const val WEBRTC_VIEW_MODEL_KEY = "webrtc_view_model"


@Composable
fun SignalingChannelTest(
    navController: NavController,
    kvsViewModel: KVSSignalingViewModel = viewModel(),
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        kvsViewModel.initialize(context)
    }


    var channelName by remember { mutableStateOf("demo-channel") }
    val scope = rememberCoroutineScope()
    val kvsState by kvsViewModel.uiState.collectAsState()
    val webRtcConfig by kvsViewModel.webRtcConfig.collectAsState()

    // 상태가 변경될 때마다 실행되는 효과
    LaunchedEffect(kvsState) {
        when (kvsState) {
            is WebRTCUiState.Success -> {
                val successState = kvsState as WebRTCUiState.Success

                val newConfig = WebRtcConfig(
                    channelName = channelName,
                    channelArn = successState.channelArn,
                    webrtcEndpoint = successState.endpointList.find { it.protocol == "HTTPS" }?.resourceEndpoint ?: "",
                    mWssEndpoint = successState.endpointList.find { it.protocol == "WSS" }?.resourceEndpoint ?: "",
                    isMaster = successState.role == ChannelRole.MASTER,
                    isFrontCamera = true,
                    isAudioEnabled = true
                )


                when (successState.role) {
                    ChannelRole.MASTER -> {
                        navController.navigate(NavRoutes.Monitoring.Master.route)
                    }
                    ChannelRole.VIEWER -> {
                        navController.navigate(NavRoutes.Monitoring.Viewer.route)
                    }
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it },
            label = { Text("채널 이름") },
            modifier = Modifier.fillMaxWidth()
        )

        // Master 버튼
        Button(
            onClick = {
                scope.launch {
                    kvsViewModel.updateSignalingChannelInfo(
                        channelName = channelName,
                        role = ChannelRole.MASTER
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("마스터로 입장")
        }

        // Viewer 버튼
        Button(
            onClick = {
                scope.launch {
                    kvsViewModel.updateSignalingChannelInfo(
                        channelName = channelName,
                        role = ChannelRole.VIEWER
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("시청자로 입장")
        }

        // 현재 상태 표시
        when (kvsState) {
            WebRTCUiState.Loading -> {
                CircularProgressIndicator()
            }
            is WebRTCUiState.Error -> {
                Text(
                    text = (kvsState as WebRTCUiState.Error).message,
                    color = Color.Red
                )
            }
            else -> {}
        }
    }
}


//webRTC테스트용 나중에 지울 것
@Composable
fun WebRTCTestScreen() {
    val context = LocalContext.current
    var testResult by remember { mutableStateOf<String?>(null) }
    var hasPermissions by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }

    // EGL 컨텍스트 생성
    val eglBase = remember { EglBase.create() }

    // SurfaceViewRenderer 생성
    val surfaceView = remember {
        SurfaceViewRenderer(context).apply {
            setMirror(false)  // 미러링 비활성화
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    // WebRTC components
    val (videoCapturer, setVideoCapturer) = remember { mutableStateOf<VideoCapturer?>(null) }
    val (videoSource, setVideoSource) = remember { mutableStateOf<VideoSource?>(null) }
    val (localVideoTrack, setLocalVideoTrack) = remember { mutableStateOf<VideoTrack?>(null) }

    // 권한 요청
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            try {
                // WebRTC 초기화
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
                )
                isInitialized = true
                testResult = "초기화 완료"
            } catch (e: Exception) {
                testResult = "초기화 실패: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    // SurfaceView 초기화
    DisposableEffect(surfaceView) {
        try {
            surfaceView.init(eglBase.eglBaseContext, null)
        } catch (e: Exception) {
            Log.e("WebRTC", "SurfaceView 초기화 실패", e)
        }

        onDispose {
            try {
                videoCapturer?.stopCapture()
                videoCapturer?.dispose()
                videoSource?.dispose()
                localVideoTrack?.dispose()
                surfaceView.release()
                eglBase.release()
            } catch (e: Exception) {
                Log.e("WebRTC", "리소스 정리 실패", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermissions && isInitialized) {
            AndroidView(
                factory = { surfaceView },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                try {
                    // PeerConnectionFactory 생성
                    val factory = PeerConnectionFactory.builder()
                        .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
                        .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
                        .createPeerConnectionFactory()

                    // 카메라 설정
                    val enumerator = Camera2Enumerator(context)
                    val deviceNames = enumerator.deviceNames

                    // 먼저 후면 카메라 시도
                    var newCapturer: VideoCapturer? = null
                    for (deviceName in deviceNames) {
                        if (enumerator.isBackFacing(deviceName)) {
                            newCapturer = enumerator.createCapturer(deviceName, null)
                            if (newCapturer != null) break
                        }
                    }

                    // 후면 카메라가 없다면 전면 카메라 시도
                    if (newCapturer == null) {
                        for (deviceName in deviceNames) {
                            if (enumerator.isFrontFacing(deviceName)) {
                                newCapturer = enumerator.createCapturer(deviceName, null)
                                if (newCapturer != null) break
                            }
                        }
                    }

                    if (newCapturer == null) {
                        throw Exception("사용 가능한 카메라를 찾을 수 없습니다")
                    }

                    setVideoCapturer(newCapturer)

                    // 비디오 소스 설정
                    val newVideoSource = factory.createVideoSource(false)
                    setVideoSource(newVideoSource)

                    val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
                    newCapturer.initialize(surfaceTextureHelper, context, newVideoSource.capturerObserver)

                    // 낮은 해상도로 시작
                    newCapturer.startCapture(640, 480, 30)

                    // 비디오 트랙 설정
                    val newVideoTrack = factory.createVideoTrack("local_track", newVideoSource)
                    setLocalVideoTrack(newVideoTrack)
                    newVideoTrack.addSink(surfaceView)

                    testResult = "카메라 스트리밍 시작됨"

                } catch (e: Exception) {
                    testResult = "테스트 실패: ${e.message}"
                    Log.e("WebRTC", "카메라 시작 실패", e)
                }
            },
            enabled = hasPermissions && isInitialized
        ) {
            Text("카메라 시작")
        }

        testResult?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center,
                color = if (it.contains("실패")) Color.Red else Color.Green,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}