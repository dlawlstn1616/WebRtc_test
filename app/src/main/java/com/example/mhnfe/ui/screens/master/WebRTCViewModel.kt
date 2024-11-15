//package com.example.mhnfe.ui.screens.master
//
//
//import android.Manifest
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.BitmapFactory
//import android.media.AudioManager
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import android.util.Base64
//import android.util.Log
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.asLiveData
//import androidx.lifecycle.viewModelScope
//import com.amazonaws.auth.AWSCredentials
//import com.amazonaws.auth.AWSCredentialsProvider
//import com.amazonaws.auth.AWSSessionCredentials
//import com.amazonaws.auth.BasicAWSCredentials
//import com.amazonaws.mobile.client.AWSMobileClient
//import com.amazonaws.regions.Region
//import com.amazonaws.regions.Regions
//import com.amazonaws.services.kinesisvideowebrtcstorage.AWSKinesisVideoWebRTCStorageClient
//import com.amazonaws.services.kinesisvideowebrtcstorage.model.JoinStorageSessionRequest
//import com.example.mhnfe.R
//import com.example.mhnfe.data.signaling.SignalingListener
//import com.example.mhnfe.data.signaling.model.Event
//import com.example.mhnfe.data.signaling.model.Message
//import com.example.mhnfe.data.signaling.okhttp.SignalingServiceWebSocketClient
//import com.example.mhnfe.utils.AwsV4Signer
//import com.example.mhnfe.utils.Constants
//import com.example.mhnfe.webrtc.KinesisVideoPeerConnection
//import com.example.mhnfe.webrtc.KinesisVideoSdpObserver
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.webrtc.AudioTrack
//import org.webrtc.Camera1Enumerator
//import org.webrtc.CameraEnumerator
//import org.webrtc.DataChannel
//import org.webrtc.DefaultVideoDecoderFactory
//import org.webrtc.DefaultVideoEncoderFactory
//import org.webrtc.EglBase
//import org.webrtc.IceCandidate
//import org.webrtc.Logging
//import org.webrtc.MediaConstraints
//import org.webrtc.MediaStream
//import org.webrtc.MediaStreamTrack
//import org.webrtc.PeerConnection
//import org.webrtc.PeerConnectionFactory
//import org.webrtc.RtpTransceiver
//import org.webrtc.SessionDescription
//import org.webrtc.SurfaceTextureHelper
//import org.webrtc.SurfaceViewRenderer
//import org.webrtc.VideoCapturer
//import org.webrtc.VideoFrame
//import org.webrtc.VideoSink
//import org.webrtc.VideoSource
//import org.webrtc.VideoTrack
//import org.webrtc.audio.JavaAudioDeviceModule
//import java.net.URI
//import java.net.URLEncoder
//import java.nio.ByteBuffer
//import java.nio.charset.Charset
//import java.util.Date
//import java.util.LinkedList
//import java.util.Optional
//import java.util.Queue
//import java.util.UUID
//import java.util.concurrent.Executors
//import java.util.concurrent.ScheduledExecutorService
//import java.util.concurrent.TimeUnit
//
//data class WebRtcConfig(
//    val channelName: String = "demo-channel",
//    val isMaster: Boolean = true,
//    val isFrontCamera: Boolean = true,
//    val isAudioEnabled: Boolean = true,
//    val channelArn: String = "",
//    val webrtcEndpoint: String = "",
//    val mWssEndpoint: String = ""
//)
//data class TurnServer(
//    val urls: List<String>,
//    val username: String,
//    val password: String
//)
//
//// ConnectionState sealed class 정의
//sealed class ConnectionState {
//    object Disconnected : ConnectionState()
//    object Connecting : ConnectionState()
//    object Connected : ConnectionState()
//    data class Error(val message: String) : ConnectionState()
//}
//
//class WebRtcViewModel(
//    private val kvsSignalingViewModel: KVSSignalingViewModel,
//    private val context: Context,
//    private val notificationManager: NotificationManager
//) : ViewModel() {
//
//    private val observer =  Observer<KvsEndpointData> { endpointData ->
//        config = WebRtcConfig(
//            webrtcEndpoint = endpointData.webrtcEndpoint,
//            mWssEndpoint = endpointData.mWssEndpoint
//        )
//    }
//
//
//    init {
//        kvsSignalingViewModel.endpointData.observeForever(observer)
//    }
//
//    //테스트
//    private val _isInitialized = MutableStateFlow(false)
//    val isInitialized: StateFlow<Boolean> get() = _isInitialized
//    private var isWebRtcInitialized = false
//    private val _isLoading = MutableStateFlow(true)
//    val isLoading: StateFlow<Boolean> = _isLoading
//    private var remoteVideoTrack: VideoTrack? = null
//    private var remoteAudioTrack: AudioTrack? = null
//    private val _localVideoTrackState = MutableStateFlow<VideoTrack?>(null)
//    val localVideoTrackState: StateFlow<VideoTrack?> = _localVideoTrackState.asStateFlow()
//
//    private val _remoteVideoTrackState = MutableStateFlow<VideoTrack?>(null)
//    val remoteVideoTrackState: StateFlow<VideoTrack?> = _remoteVideoTrackState.asStateFlow()
//
//    // ProxyVideoSink 클래스 구현
//    private class ProxyVideoSink : VideoSink {
//        private var target: VideoSink? = null
//
//        @Synchronized
//        override fun onFrame(frame: VideoFrame) {
//            target?.onFrame(frame)
//        }
//
//        @Synchronized
//        fun setTarget(target: VideoSink?) {
//            this.target = target
//        }
//    }
//
//    private val remoteProxyVideoSink = ProxyVideoSink()
//
//    private var config: WebRtcConfig? = null
//    val currentConfig = config ?: WebRtcConfig() // 기본 값을 가진 config 를 사용
//
//    // Previous properties remain the same
//    private lateinit var streamArn: String
//    private lateinit var WssEndpoint: String
//    private lateinit var webrtcEndpoint: String
//    private lateinit var clientId: String
//    private lateinit var region: String
//    private var isMaster: Boolean = true
//    private var isAudioEnabled: Boolean = false
//    private var isFrontCamera: Boolean = true
//
//    data class Size(
//        val width: Float = 0f,
//        val height: Float = 0f
//    )
//
//
//    companion object {
//        private const val TAG = "WebRtcViewModel"
//
//        private fun getCredentialsProvider(): AWSCredentialsProvider {
//            return AWSMobileClient.getInstance()
//        }
//
//
//
//        private val credentials = getCredentialsProvider().credentials
//        private val AWS_ACCESS_KEY = credentials.awsAccessKeyId
//        private val AWS_SECRET_KEY = credentials.awsSecretKey
//        private val AWS_SESSION_TOKEN: String = Optional.of(credentials)
//            .filter { creds: AWSCredentials? -> creds is AWSSessionCredentials }
//            .map { awsCredentials: AWSCredentials -> awsCredentials as AWSSessionCredentials }
//            .map { obj: AWSSessionCredentials -> obj.sessionToken }
//            .orElse("")
//        private const val AWS_REGION = "ap-northeast-2"  // 서울 리전
//
//        // WebRTC 관련 상수
//        private const val AUDIO_TRACK_ID = "KvsAudioTrack"
//        private const val VIDEO_TRACK_ID = "KvsVideoTrack"
//        private const val LOCAL_MEDIA_STREAM_LABEL = "KvsLocalMediaStream"
//        private const val VIDEO_SIZE_WIDTH = 400
//        private const val VIDEO_SIZE_HEIGHT = 300
//        private const val VIDEO_FPS = 30
//        private const val ENABLE_INTEL_VP8_ENCODER = true
//        private const val ENABLE_H264_HIGH_PROFILE = true
//
//        // View 크기 관련 상수
//        private const val LOCAL_VIEW_SIZE_RATIO = 0.25f
//        private const val REMOTE_VIEW_SIZE_RATIO = 0.75f
//        private const val MIN_VIEW_SIZE = 100f // dp
//        private const val MAX_VIEW_SIZE = 1000f // dp
//        private const val DEFAULT_VIEW_PADDING = 8 // dp
//
//        // 드래그 관련 상수
//        private const val DRAG_THRESHOLD = 10f // dp
//        private const val EDGE_MARGIN = 16 // dp
//
//        // Data Channel 관련 상수
//        private const val DATA_CHANNEL_NAME = "data-channel"
//        private const val MAX_MESSAGE_LENGTH = 1000
//
//        // 알림 관련 상수
//        const val CHANNEL_ID = "WebRtcNotificationChannel"
//        private const val NOTIFICATION_TITLE = "Message from Peer!"
//        private const val NOTIFICATION_PRIORITY = NotificationCompat.PRIORITY_HIGH
//
//        // WebSocket 관련 상수
//        private const val WS_CLOSE_TIMEOUT_MS = 1000L
//        private const val STATS_DELAY_SECONDS = 10L
//
//        // AWS 관련 Query Parameter 상수
//        private const val CHANNEL_ARN_QUERY_PARAM = "X-Amz-ChannelARN"
//        private const val CLIENT_ID_QUERY_PARAM = "X-Amz-ClientId"
//    }
//
//    private fun logCredentials() {
//        Log.d("CredentialsLog", "AWS_ACCESS_KEY: $AWS_ACCESS_KEY")
//        Log.d("CredentialsLog", "AWS_SECRET_KEY: ${AWS_SECRET_KEY.take(4)}********") // 앞 4자리만 표시
//        Log.d("CredentialsLog", "AWS_SESSION_TOKEN: ${AWS_SESSION_TOKEN.take(10)}********") // 앞 10자리만 표시
//    }
//
//
//    // WebRtc의 UI 상태
//    data class WebRtcUiState(
//        val connectionState: ConnectionState = ConnectionState.Disconnected,
//        val isDataChannelOpen: Boolean = false,
//        val hasAudioPermission: Boolean = false,
//        val hasVideoPermission: Boolean = false,
//        val isCameraFront: Boolean = true,
//        val recordingInfo: String? = null
//    )
//
//    private var videoCapturer: VideoCapturer? = null
//    private lateinit var videoSource: VideoSource
//    private lateinit var localVideoTrack: VideoTrack
//
//    private var client: SignalingServiceWebSocketClient? = null
//    private lateinit var peerConnectionFactory: PeerConnectionFactory
//
//    private lateinit var audioManager: AudioManager
//    private var originalAudioMode: Int = 0
//    private var originalSpeakerphoneOn: Boolean = false
//
//    private lateinit var localAudioTrack: AudioTrack
//
//    private var localView: SurfaceViewRenderer? = null
//    private var remoteView: SurfaceViewRenderer? = null
//
//    private var localPeer: PeerConnection? = null
//
//    private var rootEglBase: EglBase? = null
//
//    private val peerIceServers = mutableListOf<PeerConnection.IceServer>()
//
//    private var gotException = false
//
//    private lateinit var recipientClientId: String
//
//    private var master = true
//    private var isAudioSent = false
//
//    private lateinit var mChannelArn: String
//    private lateinit var mClientId: String
//    private lateinit var mStreamArn: String
//
//    private lateinit var mWssEndpoint: String
//    private lateinit var mRegion: String
//
//    private var mCameraFacingFront = true
//
//    private var mCreds: AWSCredentials = BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
//
//    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
//    val connectionState: StateFlow<ConnectionState> = _connectionState
//
//    private val _dataChannelMessage = MutableStateFlow<String>("")
//    val dataChannelMessage: StateFlow<String> = _dataChannelMessage
//    private val _uiState = MutableStateFlow(WebRtcUiState())
//    val uiState: StateFlow<WebRtcUiState> = _uiState.asStateFlow()
//
//    private val _localViewSize = MutableStateFlow(Size(0f, 0f))
//    val localViewSize: StateFlow<Size> = _localViewSize.asStateFlow()
//
//    private val _remoteViewSize = MutableStateFlow(Size(0f, 0f))
//    val remoteViewSize: StateFlow<Size> = _remoteViewSize.asStateFlow()
//    fun getEglContext(): EglBase.Context? {
//        return rootEglBase?.eglBaseContext
//    }
//
//
//    // 상태 업데이트 함수들
//    private fun updateConnectionState(newState: ConnectionState) {
//        viewModelScope.launch {
//            _uiState.update { currentState ->
//                currentState.copy(
//                    connectionState = newState
//                )
//            }
//        }
//    }
//
//    // P2P 연결을 담당 하는 Data channel
//    private fun updateDataChannelState(isOpen: Boolean) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isDataChannelOpen = isOpen) }
//        }
//    }
//
//    fun updateDataChannelMessage(message: String) {
//        viewModelScope.launch {
//            _dataChannelMessage.value = message
//        }
//    }
//
//    private fun updateRecordingInfo(info: String?) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(recordingInfo = info) }
//        }
//    }
//
//    private fun updatePermissionState(hasAudio: Boolean? = null, hasVideo: Boolean? = null) {
//        viewModelScope.launch {
//            _uiState.update { currentState ->
//                currentState.copy(
//                    hasAudioPermission = hasAudio ?: currentState.hasAudioPermission,
//                    hasVideoPermission = hasVideo ?: currentState.hasVideoPermission
//                )
//            }
//        }
//    }
//
//    private fun updateCameraFacing(isFront: Boolean) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isCameraFront = isFront) }
//        }
//    }
//
//    private fun updateViewSizes() {
//        val displayMetrics = context.resources.displayMetrics
//        viewModelScope.launch {
//            _localViewSize.value = Size(
//                displayMetrics.widthPixels * LOCAL_VIEW_SIZE_RATIO,
//                displayMetrics.heightPixels * LOCAL_VIEW_SIZE_RATIO
//            )
//            _remoteViewSize.value = Size(
//                displayMetrics.widthPixels * REMOTE_VIEW_SIZE_RATIO,
//                displayMetrics.heightPixels * REMOTE_VIEW_SIZE_RATIO
//            )
//        }
//    }
//
//    // 권한 체크 및 요청
//    private suspend fun checkAndRequestPermissions(): Boolean = withContext(Dispatchers.Main) {
//        val permissions = mutableListOf(
//            Manifest.permission.CAMERA,
//            Manifest.permission.MODIFY_AUDIO_SETTINGS
//        )
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
//        }
//
//        val hasAllPermissions = permissions.all { permission ->
//            ContextCompat.checkSelfPermission(context, permission) ==
//                    PackageManager.PERMISSION_GRANTED
//        }
//
//        updatePermissionState(
//            hasAudio = ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.MODIFY_AUDIO_SETTINGS
//            ) == PackageManager.PERMISSION_GRANTED,
//            hasVideo = ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED
//        )
//
//        hasAllPermissions
//    }
//    /**
//     * Prints WebRTC stats to the debug console every so often.
//     */
//    private val printStatsExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
//
//    /**
//     * Mapping of established peer connections to the peer's sender id.
//     */
//    private val peerConnectionFoundMap = mutableMapOf<String, PeerConnection>()
//    private val pendingIceCandidatesMap = mutableMapOf<String, Queue<IceCandidate>>()
//
//    fun initialize(configData: WebRtcConfig) {
//        Log.d(TAG, "Initializing WebRTC components")
//
//        // config 초기화
//        config = configData
//        logCredentials()
//
//
//        // 필수 필드 설정
//        this.mChannelArn = configData.channelArn
//        this.webrtcEndpoint = configData.webrtcEndpoint
//        this.mWssEndpoint = configData.mWssEndpoint
//        this.isMaster = configData.isMaster
//        this.isFrontCamera = configData.isFrontCamera
//        this.isAudioEnabled = configData.isAudioEnabled
//
//
//        viewModelScope.launch {
//            try {
//                this@WebRtcViewModel.clientId = UUID.randomUUID().toString()
//                this@WebRtcViewModel.region = AWS_REGION
//
//                // 설정 유효성 검사
//                requireNotNull(config) { "WebRTC config is required" }
//                require(config?.webrtcEndpoint?.isNotBlank() == true) { "WebRTC endpoint is required" }
//                require(config?.mWssEndpoint?.isNotBlank() == true) { "WSS endpoint is required" }
//
//                _isInitialized.value = true  // 초기화 플래그 설정
//
//                // 권한 체크
//                if (!checkAndRequestPermissions()) {
//                    Log.e(TAG, "Permissions not granted. Aborting initialization.")
//                    updateConnectionState(ConnectionState.Error("Required permissions not granted"))
//                    _isLoading.value = false
//                    return@launch
//                }
//
//                Log.d(TAG, "Permissions granted")
//                updateConnectionState(ConnectionState.Connecting)
//
//                // WebRTC 초기화
//                Log.d(TAG, "Initializing WebRTC")
//                initializeWebRtc(context)
//                isWebRtcInitialized = true
//                Log.d(TAG, "WebRTC initialized")
//
////                // 비디오 컴포넌트 초기화
//                Log.d(TAG, "Initializing video components")
//                if (isWebRtcInitialized) {
//                    initializeVideoComponents()
//                } else {
//                    Log.e(TAG, "WebRTC initialization not completed - Video components initialization skipped")
//                }
//                Log.d(TAG, "Video components initialized")
//
////                // View 크기 초기화
////                Log.d(TAG, "Updating view sizes")
////                updateViewSizes()
////                Log.d(TAG, "View sizes updated")
//
//                // WebSocket 연결 초기화
//                Log.d(TAG, "Initializing WebSocket connection")
//                initWsConnection()
//                Log.d(TAG, "WebSocket connection initialized")
//
//                _isLoading.value = false
//                Log.d(TAG, "Initialization complete")
//                Log.d(TAG, "WebRTC components initialized successfully")
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Initialize failed", e)
//                _isInitialized.value = false
//                updateConnectionState(ConnectionState.Error("초기화 실패: ${e.message}"))
//                _isLoading.value = false
//            }
//        }
//    }
//
//    private fun initWsConnection() {
//        viewModelScope.launch {
//            try {
//                mClientId = UUID.randomUUID().toString()
//
//                val wssEndpoint = config?.mWssEndpoint ?: throw IllegalStateException("WSS endpoint is not configured")
//                Log.d(TAG, "Original WSS endpoint: $wssEndpoint")
//
//                // 기본 엔드포인트 확인
//                if (wssEndpoint.isBlank()) {
//                    Log.e(TAG, "WSS endpoint is empty")
//                    updateConnectionState(ConnectionState.Error("WSS endpoint is not configured"))
//                    return@launch
//                }
//
//                // KVS ViewModel에서 WSS 엔드포인트 받아오기
//                if (config == null || config?.mWssEndpoint.isNullOrEmpty()) {
//                    Log.e(TAG, "WSS endpoint is null or empty")
//                    updateConnectionState(ConnectionState.Error("WSS endpoint is not configured"))
//                    return@launch
//                }
//
//                // WSS 엔드포인트 설정
//                mWssEndpoint = config!!.mWssEndpoint
//
//                // 올바른 WSS URL 형식 확인
//                val baseEndpoint = mWssEndpoint.trim()
//                    .removePrefix("wss://")
//                    .removeSuffix("/")
//
//                // Endpoint URL 생성
//                val endpoint = if (isMaster) {
//                    "$wssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=${mChannelArn.encodeUrl()}"
//                } else {
//                    "$wssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=${mChannelArn.encodeUrl()}&${Constants.CLIENT_ID_QUERY_PARAM}=${mClientId.encodeUrl()}"
//                }
//
//                Log.d("initWsConnection", "mWssEndpoint: $wssEndpoint")
//                Log.d("initWsConnection", "Constants.CHANNEL_ARN_QUERY_PARAM: ${Constants.CHANNEL_ARN_QUERY_PARAM}")
//                Log.d("initWsConnection", "mChannelArn: $mChannelArn")
//
//                if (isMaster) {
//                    Log.d("initWsConnection", "masterEndpoint: $endpoint")
//                } else {
//                    Log.d("initWsConnection", "Constants.CLIENT_ID_QUERY_PARAM: ${Constants.CLIENT_ID_QUERY_PARAM}")
//                    Log.d("initWsConnection", "mClientId: $mClientId")
//                    Log.d("initWsConnection", "viewerEndpoint: $endpoint")
//                }
//
//                // URI 서명
//                val signedUri = getSignedUri(endpoint)?.also {
//                    Log.d(TAG, "Signed URI: $it")
//                } ?: run {
//                    Log.e(TAG, "Failed to get signed URI")
//                    updateConnectionState(ConnectionState.Error("Failed to get signed URI"))
//                    return@launch
//                }
//
//
//
//                // WebSocket 클라이언트 생성 및 연결
//                withContext(Dispatchers.IO) {
//                    try {
//                        Log.d(TAG, "Initializing WebSocket client with URI: $signedUri")
//                        client = SignalingServiceWebSocketClient(
//                            signedUri.toString(),
//                            createSignalingListener(),
//                            Executors.newFixedThreadPool(10)
//                        )
//
//                        if (client?.isOpen() == true) {
//                            Log.d(TAG, "WebSocket connection successful")
//
//                            if (isMaster) {
//                                createLocalPeerConnection()
////                                handleMasterConnection()
//                            } else {
//                                createSdpOffer()
//                            }
//
//                            updateConnectionState(ConnectionState.Connected)
//                        } else {
//                            updateConnectionState(ConnectionState.Error("WebSocket connection failed"))
//                        }
//                    } catch (e: Exception) {
//                        Log.e(TAG, "WebSocket connection error", e)
//                        updateConnectionState(ConnectionState.Error("WebSocket connection error: ${e.message}"))
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to initialize WebSocket connection", e)
//                updateConnectionState(ConnectionState.Error("초기화 실패: ${e.message}"))
//            }
//        }
//    }
//
//    // URL 인코딩 확장 함수
//    private fun String.encodeUrl(): String {
//        return URLEncoder.encode(this, "UTF-8")
//    }
//
//    private fun isValidClient(): Boolean {
//        // isOpen을 프로퍼티로 접근
//        return client?.isOpen() == true
//    }
//
//    /**
//     * 로컬 피어 연결 생성
//     */
//    private fun createLocalPeerConnection() {
//        val currentConfig = config ?: throw IllegalStateException("WebRTC not initialized2222")
//
//        // ICE 서버 설정
//        setupIceServers()
//
//        // RTCConfiguration 설정
//        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers).apply {
//            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
//            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
//            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
//            keyType = PeerConnection.KeyType.ECDSA
//            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
//            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
//
//            // ICE 관련 최적화 설정
//            iceTransportsType = PeerConnection.IceTransportsType.ALL
//            candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.ALL
//
//            // 미디어 최적화 설정
//            enableCpuOveruseDetection = true  // CPU 과부하 감지
//
//            // 선택적 설정
//            // sdpSemantics가 UNIFIED_PLAN일 때 사용
//            enableImplicitRollback = true  // 암시적 롤백 지원
//
//            // 타임아웃 설정
//            iceConnectionReceivingTimeout = 30000  // ICE 연결 수신 타임아웃 (ms)
//            iceBackupCandidatePairPingInterval = 2000  // ICE 백업 후보 ping 간격 (ms)
//
//            // 모바일 네트워크 최적화
//            activeResetSrtpParams = true  // SRTP 파라미터 재설정 활성화
//        }
//
//        // PeerConnection 생성
//        // PeerConnection 생성
//        localPeer = peerConnectionFactory.createPeerConnection(
//            rtcConfig,
//            object : KinesisVideoPeerConnection() {
//                override fun onIceCandidate(iceCandidate: IceCandidate) {
//                    super.onIceCandidate(iceCandidate)
//                    handleIceCandidate(iceCandidate)
//                }
//
//                override fun onTrack(transceiver: RtpTransceiver) {
//                    val track = transceiver.receiver.track()
//                    when (track?.kind()) {
//                        MediaStreamTrack.VIDEO_TRACK_KIND -> {
//                            if (track is VideoTrack) {
//                                _remoteVideoTrackState.value = track
//                                Log.d(TAG, "Remote video track received and set")
//                            }
//                        }
//                    }
//                }
//
//                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
//                    super.onIceConnectionChange(iceConnectionState)
//                    handleIceConnectionChange(iceConnectionState)
//                }
//
//                // 추가된 콜백들
//                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
//                    super.onConnectionChange(newState)
//                }
//
//                override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
//                    super.onSignalingChange(signalingState)
//                }
//
//                override fun onRenegotiationNeeded() {
//                    super.onRenegotiationNeeded()
//                }
//            }
//        )
//
//        // 스트림 및 데이터 채널 추가
//        if (localPeer != null) {
//            addTracksToLocalPeer()
//            addDataChannelToLocalPeer()
//            startStatsCollection()
//        } else {
//            updateConnectionState(ConnectionState.Error("Failed to create peer connection"))
//        }
//    }
//
//    private val _remoteVideoTrackAvailable = MutableStateFlow(false)
//    val remoteVideoTrackAvailable: StateFlow<Boolean> = _remoteVideoTrackAvailable.asStateFlow()
//
//    private val _remoteAudioTrackAvailable = MutableStateFlow(false)
//    val remoteAudioTrackAvailable: StateFlow<Boolean> = _remoteAudioTrackAvailable.asStateFlow()
//
//
//    // 오디오 트랙 처리 함수
//    private fun handleRemoteAudioTrack(audioTrack: AudioTrack) {
//        try {
//            remoteAudioTrack = audioTrack
//            remoteAudioTrack?.setEnabled(true)
//
//            // UI 업데이트를 위한 이벤트 발생
//            _remoteAudioTrackAvailable.value = true
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling remote audio track: ${e.message}")
//            _remoteAudioTrackAvailable.value = false
//        }
//    }
//
//
//    // SurfaceViewRenderer 설정 메서드
//    fun setRemoteVideoRenderer(renderer: SurfaceViewRenderer) {
//        remoteProxyVideoSink.setTarget(renderer)
//    }
//
//    /**
//     * ICE 서버 설정
//     */
//    private fun setupIceServers() {
//        val currentConfig = config ?: return
//
//        try {
//            // STUN 서버 추가
//            val stunUrl = "stun:stun.kinesisvideo.${region}.amazonaws.com:443"
//            val stunServer = PeerConnection.IceServer.builder(stunUrl).createIceServer()
//            peerIceServers.add(stunServer)
//            Log.d(TAG, "Added STUN server: $stunUrl")
//
//            // TURN 서버 추가 (선택적)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error setting up ICE servers", e)
//            updateConnectionState(ConnectionState.Error("ICE 서버 설정 실패: ${e.message}"))
//        }
//    }
//    /**
//     * ICE Candidate 처리
//     */
//    private fun handleIceCandidate(iceCandidate: IceCandidate) {
//        val message = createIceCandidateMessage(iceCandidate)
//        Log.d(TAG, "Sending IceCandidate to remote peer $iceCandidate")
//        client?.sendIceCandidate(message)
//    }
//    /**
//     * 원격 스트림 처리
//     */
//    private fun handleRemoteStream(mediaStream: MediaStream) {
//        Log.d(TAG, "Adding remote video stream (and audio) to the view")
//
//        viewModelScope.launch(Dispatchers.Main) {
//            try {
//                // 비디오 트랙 처리
//                mediaStream.videoTracks?.firstOrNull()?.let { videoTrack ->
//                    Log.d(TAG, "Remote video track received: ${videoTrack.id()}")
//                    videoTrack.addSink(remoteView)
//                }
//
//                // 오디오 트랙 처리
//                mediaStream.audioTracks?.firstOrNull()?.let { audioTrack ->
//                    Log.d(TAG, "Remote audio track received: ${audioTrack.id()}")
//                    audioTrack.setEnabled(true)
//                    setAudioConfiguration()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error handling remote stream", e)
//                updateConnectionState(ConnectionState.Error("Remote stream error: ${e.message}"))
//            }
//        }
//    }
//
//    /**
//     * ICE 연결 상태 변경 처리
//     */
//    private fun handleIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
//        when (iceConnectionState) {
//            PeerConnection.IceConnectionState.CONNECTED -> {
//                updateConnectionState(ConnectionState.Connected)
//            }
//            PeerConnection.IceConnectionState.FAILED -> {
//                updateConnectionState(ConnectionState.Error("ICE connection failed"))
//            }
//            PeerConnection.IceConnectionState.DISCONNECTED -> {
//                updateConnectionState(ConnectionState.Disconnected)
//            }
//            else -> {
//                Log.d(TAG, "ICE connection state changed to: $iceConnectionState")
//            }
//        }
//    }
//
//    /**
//     * 데이터 채널 메시지 처리
//     */
//    private fun handleDataChannelMessage(message: String) {
//        viewModelScope.launch {
//            showNotification(message)
//            _dataChannelMessage.value = message
//        }
//    }
//
//    /**
//     * Stats 수집 시작
//     */
//    private fun startStatsCollection() {
//        localPeer?.let { peer ->
//            printStatsExecutor.scheduleWithFixedDelay(
//                {
//                    peer.getStats { rtcStatsReport ->
//                        rtcStatsReport.statsMap.forEach { (key, value) ->
//                            Log.d(TAG, "Stats: $key, $value")
//                        }
//                    }
//                },
//                0,
//                STATS_DELAY_SECONDS,
//                TimeUnit.SECONDS
//            )
//        }
//    }
//
//    /**
//     * 오디오 설정
//     */
//    private fun setAudioConfiguration() {
//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
//        audioManager.isSpeakerphoneOn = true
//    }
//
//    /**
//     * 알림 표시
//     */
//    private fun showNotification(message: String) {
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentTitle("New WebRTC Message")
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(context)
//                .notify(System.currentTimeMillis().toInt(), builder.build())
//        }
//    }
//
//
//
//    private fun initializeWebRtc(context: Context) {
//        if (isWebRtcInitialized) {
//            Log.d(TAG, "WebRTC is already initialized")
//            return
//        }
//
//        Log.d(TAG, "Starting WebRTC initialization")
//
//        if (config == null) {
//            Log.e(TAG, "컨피그 화면 데이터 전송 실패")
//            config = currentConfig
//        }
//
//        try {
//            rootEglBase = EglBase.create()
//            Log.d(TAG, "EglBase created")
//
//            PeerConnectionFactory.initialize(
//                PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions()
//            )
//            Log.d(TAG, "PeerConnectionFactory initialized")
//
//            val vdf = DefaultVideoDecoderFactory(rootEglBase?.eglBaseContext)
//            val vef = DefaultVideoEncoderFactory(rootEglBase?.eglBaseContext, ENABLE_INTEL_VP8_ENCODER, ENABLE_H264_HIGH_PROFILE)
//
//            peerConnectionFactory = PeerConnectionFactory.builder()
//                .setVideoDecoderFactory(vdf)
//                .setVideoEncoderFactory(vef)
//                .setAudioDeviceModule(JavaAudioDeviceModule.builder(context).createAudioDeviceModule())
//                .createPeerConnectionFactory()
//
//            Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO)
//            Log.d(TAG, "Logging enabled")
//
//            if (_isInitialized.value) {
//                videoCapturer = createVideoCapturer()
//                Log.d(TAG, "Video capturer created")
//            } else {
//                Log.w(TAG, "Initialization flag is false; skipping video capturer creation")
//            }
//            isWebRtcInitialized = true
//            val signalingEndpoint = "your_signaling_endpoint_here" // 실제 설정된 Endpoint 값으로 대체
////            viewModelScope.launch {
////                handleStorageSession()
////            }
//
//            isWebRtcInitialized = true
//            Log.d(TAG, "WebRTC fully initialized")
//        } catch (e: Exception) {
//            Log.e(TAG, "WebRTC initialization failed", e)
//        }
//    }
//
//    /**
//     * 비디오 캡처러 생성
//     */
//    private fun createVideoCapturer(): VideoCapturer {
//        val currentConfig = config ?: throw IllegalStateException("WebRTC not initialized")
//        Log.d(TAG, "Using camera config: ${currentConfig.isFrontCamera}")
//        return createCameraCapturer(Camera1Enumerator(false), currentConfig.isFrontCamera)
//    }
//
//
//    /**
//     * SignalingListener 구현
//     */
//    private fun createSignalingListener() = object : SignalingListener() {
//        override fun onSdpOffer(offerEvent: Event) {
//            Log.d(TAG, "Received SDP Offer: Setting Remote Description")
//
//            val sdp = Event.parseOfferEvent(offerEvent)
//
//            localPeer?.setRemoteDescription(
//                KinesisVideoSdpObserver(),
//                SessionDescription(SessionDescription.Type.OFFER, sdp)
//            )
//
//            recipientClientId = offerEvent.senderClientId
//            Log.d(TAG, "Received SDP offer for client ID: $recipientClientId. Creating answer")
//
//            createSdpAnswer()
//
//            if (master && webrtcEndpoint != null) {
//                viewModelScope.launch {
//                    _uiState.update {
//                        it.copy(recordingInfo = "Media is being recorded to $mStreamArn")
//                    }
//                }
//                Log.i(TAG, "Media is being recorded to $mStreamArn")
//            }
//        }
//
//        override fun onSdpAnswer(answerEvent: Event) {
//            Log.d(TAG, "SDP answer received from signaling")
//
//            val sdp = Event.parseSdpEvent(answerEvent)
//            val sdpAnswer = SessionDescription(SessionDescription.Type.ANSWER, sdp)
//
//            localPeer?.let { peer ->
//                peer.setRemoteDescription(
//                    object : KinesisVideoSdpObserver() {
//                        override fun onCreateFailure(error: String) {
//                            super.onCreateFailure(error)
//                            updateConnectionState(ConnectionState.Error("SDP answer error: $error"))
//                        }
//                    },
//                    sdpAnswer
//                )
//
//                Log.d(TAG, "Answer Client ID: ${answerEvent.senderClientId}")
//                peerConnectionFoundMap[answerEvent.senderClientId] = peer
//                handlePendingIceCandidates(answerEvent.senderClientId)
//            }
//        }
//
//        override fun onIceCandidate(message: Event) {
//            Log.d(TAG, "Received ICE candidate from remote")
//            val iceCandidate = Event.parseIceCandidate(message)
//            if (iceCandidate != null) {
//                checkAndAddIceCandidate(message, iceCandidate)
//            } else {
//                Log.e(TAG, "Invalid ICE candidate: $message")
//            }
//        }
//
//        override fun onError(errorMessage: Event) {
//            Log.e(TAG, "Received error message: $errorMessage")
//            updateConnectionState(ConnectionState.Error(errorMessage.toString()))
//        }
//
//        override fun onException(e: Exception) {
//            Log.e(TAG, "Signaling client returned exception: ${e.message}")
//            updateConnectionState(ConnectionState.Error("Signaling exception: ${e.message}"))
//        }
//    }
//
//    private fun handleMasterConnection() {
//
//        if (webrtcEndpoint == null) {
//            Log.e(TAG, "WebRTC endpoint is not set, unable to proceed with storage session")
//            return
//        }
//
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//
//                AWSMobileClient.getInstance().refresh()
//                Log.d("CredentialsLog", "AWS_ACCESS_KEY: $AWS_ACCESS_KEY")
//                Log.d("CredentialsLog", "AWS_SECRET_KEY: ${AWS_SECRET_KEY.take(4)}********") // 앞 4자리만 표시
//                Log.d("CredentialsLog", "AWS_SESSION_TOKEN: ${AWS_SESSION_TOKEN.take(10)}********") // 앞 10자리만 표시
//                val storageClient = AWSKinesisVideoWebRTCStorageClient(mCreds).apply {
//                    AWS_SESSION_TOKEN
//                    setRegion(Region.getRegion(AWS_REGION))
//                    setSignerRegionOverride(AWS_REGION)
//                    setServiceNameIntern("kinesisvideo")
//                    // 엔드포인트를 필요할 때만 설정
//                    if (endpoint != webrtcEndpoint) {
//                        setEndpoint(webrtcEndpoint!!)
//                        Log.d(TAG, "Endpoint set to: $webrtcEndpoint")
//                    }
//                }
//                Log.d(TAG, "Endpoint set to: $webrtcEndpoint")
//
//
//
//                Log.i(TAG, "Channel ARN is: $mChannelArn")
//                Log.i(TAG, "Channel ARN is: $credentials")
//                AWSMobileClient.getInstance().refresh()
//                storageClient.joinStorageSession(
//                    JoinStorageSessionRequest().withChannelArn(mChannelArn)
//
//                )
//                Log.d(TAG, "AWS_REGION: $AWS_REGION")
//                Log.d(TAG, "Channel ARN: $mChannelArn")
//
//                Log.i(TAG, "Join storage session request sent!")
//            } catch (ex: Exception) {
//                Log.e(TAG, "Error sending join storage session request!", ex)
//                updateConnectionState(ConnectionState.Error("Storage session error: ${ex.message}"))
//            }
//        }
//    }
//
//
//    private fun getSignedUri(endpoint: String): URI? {
//        try {
//            return AwsV4Signer.sign(
//                URI.create(endpoint),
//                AWS_ACCESS_KEY,
//                AWS_SECRET_KEY,
//                AWS_SESSION_TOKEN,
//                URI.create(mWssEndpoint),
//                AWS_REGION,
//                Date().time
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to sign URI", e)
//            updateConnectionState(ConnectionState.Error("Failed to sign URI: ${e.message}"))
//            return null
//        }
//    }
//
//    /**
//     * 스토리지 세션 처리
//     */
//    private suspend fun handleStorageSession() = withContext(Dispatchers.IO) {
//        try {
//            val credentials = mCreds ?: return@withContext
//
//            val storageClient = AWSKinesisVideoWebRTCStorageClient(credentials).apply {
//                setRegion(Region.getRegion(Regions.AP_NORTHEAST_2))
//                setSignerRegionOverride("ap-northeast-2")
//                setServiceNameIntern("kinesisvideo")
//                setEndpoint(webrtcEndpoint)
//            }
//
//            Log.i(TAG, "Channel ARN is: ${mChannelArn}")
//
//
//
//            storageClient.joinStorageSession(
//                JoinStorageSessionRequest().withChannelArn(mChannelArn)
//            )
//            Log.i(TAG, "Join storage session request sent!")
//        } catch (ex: Exception) {
//            Log.e(TAG, "Error sending join storage session request!", ex)
//            updateConnectionState(ConnectionState.Error("Storage session error: ${ex.message}"))
//        }
//    }
//
//    // ... Rest of the WebRTC related functions (createVideoCapturer, initWsConnection, etc.)
//    // These functions remain largely the same as in the original code,
//    // just moved to the ViewModel and adapted to use StateFlow for state management
//
//    /**
//     * 비디오 캡처러 생성
//     */
//    private fun createVideoCapturer(isFrontCamera: Boolean): VideoCapturer {
//        val enumerator = Camera1Enumerator(false)
//        val deviceNames = enumerator.deviceNames
//
//        // 적절한 카메라를 찾아 캡처러 생성
//        val capturer = deviceNames.firstNotNullOfOrNull { deviceName ->
//            val isFacingDesiredDirection = if (isFrontCamera) {
//                enumerator.isFrontFacing(deviceName)
//            } else {
//                enumerator.isBackFacing(deviceName)
//            }
//
//            if (isFacingDesiredDirection) {
//                Log.d(TAG, "Creating capturer for camera: $deviceName")
//                enumerator.createCapturer(deviceName, null)
//            } else null
//        } ?: run {
//            // 원하는 방향의 카메라를 찾지 못한 경우 사용 가능한 첫 번째 카메라 사용
//            deviceNames.firstNotNullOfOrNull { deviceName ->
//                Log.d(TAG, "Falling back to available camera: $deviceName")
//                enumerator.createCapturer(deviceName, null)
//            }
//        }
//
//        return capturer ?: throw IllegalStateException("No camera available")
//    }
//
//    private fun createCameraCapturer(
//        enumerator: CameraEnumerator,
//        isFrontCamera: Boolean
//    ): VideoCapturer {
//        val deviceNames = enumerator.deviceNames
//
//        // 디바이스 정보 로깅
//        Log.d(TAG, "Available cameras:")
//        deviceNames.forEach { deviceName ->
//            Log.d(TAG, "Camera: $deviceName")
//            Log.d(TAG, "- isFrontFacing: ${enumerator.isFrontFacing(deviceName)}")
//            Log.d(TAG, "- isBackFacing: ${enumerator.isBackFacing(deviceName)}")
//        }
//
//        // 원하는 방향의 카메라 찾기
//        val capturer = deviceNames.firstNotNullOfOrNull { deviceName ->
//            val isFacingDesiredDirection = if (isFrontCamera) {
//                enumerator.isFrontFacing(deviceName)
//            } else {
//                enumerator.isBackFacing(deviceName)
//            }
//
//            if (isFacingDesiredDirection) {
//                Log.d(TAG, "Creating capturer for camera: $deviceName")
//                enumerator.createCapturer(deviceName, null)
//            } else null
//        } ?: run {
//            // 원하는 방향의 카메라를 찾지 못한 경우 사용 가능한 첫 번째 카메라 사용
//            Log.d(TAG, "Desired camera not found, using first available camera")
//            deviceNames.firstNotNullOfOrNull { deviceName ->
//                enumerator.createCapturer(deviceName, null)
//            }
//        }
//
//        return capturer ?: throw IllegalStateException("No camera available")
//    }
//
//
//    private var mNotificationId = 0
//    private var dataChannel: DataChannel? = null
//
//    private val _isDataChannelOpen = MutableStateFlow(false)
//    val isDataChannelOpen: StateFlow<Boolean> = _isDataChannelOpen
//
//    private val configObserver = Observer<WebRtcConfig?> { config ->
//        if (config != null) {
//            this.config = config
//            this.mChannelArn = config.channelArn
//            this.mChannelArn = config.channelArn
//            this.webrtcEndpoint = config.webrtcEndpoint
//            this.mWssEndpoint = config.mWssEndpoint
//            this.isMaster = config.isMaster
//            this.isFrontCamera = config.isFrontCamera
//            this.isAudioEnabled = config.isAudioEnabled
//        }
//    }
//
//    init {
//        createNotificationChannel()
//    }
//
//    private fun onDataChannel(dataChannel: DataChannel) {
//        dataChannel.registerObserver(object : DataChannel.Observer {
//            override fun onBufferedAmountChange(amount: Long) {
//                // no op on receiver side
//            }
//
//            override fun onStateChange() {
//                Log.d(TAG, "Remote Data Channel onStateChange: state: ${dataChannel.state()}")
//            }
//
//            override fun onMessage(buffer: DataChannel.Buffer) {
//                val bytes = if (buffer.data.hasArray()) {
//                    buffer.data.array()
//                } else {
//                    ByteArray(buffer.data.remaining()).also { bytes ->
//                        buffer.data.get(bytes)
//                    }
//                }
//
//                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setLargeIcon(
//                        BitmapFactory.decodeResource(
//                            context.resources,
//                            R.mipmap.ic_launcher
//                        )
//                    )
//                    .setContentTitle("Message from Peer!")
//                    .setContentText(String(bytes, Charset.defaultCharset()))
//                    .setPriority(NotificationCompat.PRIORITY_MAX)
//                    .setAutoCancel(true)
//
//                if (ActivityCompat.checkSelfPermission(
//                        context,
//                        Manifest.permission.POST_NOTIFICATIONS
//                    ) == PackageManager.PERMISSION_GRANTED
//                ) {
//                    NotificationManagerCompat.from(context)
//                        .notify(mNotificationId++, builder.build())
//                }
//
//                // Toast는 Main thread에서 실행되어야 하므로 handler 사용
//                Handler(Looper.getMainLooper()).post {
//                    Toast.makeText(
//                        context,
//                        "New message from peer, check notification.",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        })
//    }
//
//    private fun createIceCandidateMessage(iceCandidate: IceCandidate): Message {
//        val messagePayload = """
//            {
//                "candidate":"${iceCandidate.sdp}",
//                "sdpMid":"${iceCandidate.sdpMid}",
//                "sdpMLineIndex":${iceCandidate.sdpMLineIndex}
//            }
//        """.trimIndent()
//
//        val senderClientId = if (master) "" else mClientId
//
//        return Message(
//            "ICE_CANDIDATE",
//            recipientClientId,
//            senderClientId,
//            Base64.encode(
//                messagePayload.toByteArray(),
//                Base64.URL_SAFE or Base64.NO_WRAP
//            ).toString()
//        )
//    }
//
//    /**
//     * 로컬 스트림 추가
//     */
//    /**
//     * 로컬 트랙 추가
//     */
//    private fun addTracksToLocalPeer() {
//        try {
//            // 비디오 트랙 추가
//            localVideoTrack?.let { videoTrack ->
//                localPeer?.addTrack(videoTrack, listOf(LOCAL_MEDIA_STREAM_LABEL))?.also {
//                    Log.d(TAG, "Added video track to peer connection")
//                } ?: run {
//                    Log.e(TAG, "Failed to add video track to peer connection")
//                }
//            }
//
//            // 오디오 트랙 추가 (설정된 경우)
//            if (config?.isAudioEnabled == true) {
//                if (!::localAudioTrack.isInitialized) {
//                    initializeAudioComponents()
//                }
//
//                localAudioTrack?.let { audioTrack ->
//                    localPeer?.addTrack(audioTrack, listOf(LOCAL_MEDIA_STREAM_LABEL))?.also {
//                        Log.d(TAG, "Added audio track to peer connection")
//                    } ?: run {
//                        Log.e(TAG, "Failed to add audio track to peer connection")
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error adding tracks to peer connection: ${e.message}")
//        }
//    }
//
//
//    private fun addDataChannelToLocalPeer() {
//        Log.d(TAG, "Data channel addDataChannelToLocalPeer")
//
//        dataChannel = localPeer?.createDataChannel("data-channel-of-$mClientId", DataChannel.Init())?.apply {
//            registerObserver(object : DataChannel.Observer {
//                override fun onBufferedAmountChange(amount: Long) {
//                    Log.d(TAG, "Local Data Channel onBufferedAmountChange called with amount $amount")
//                }
//
//                override fun onStateChange() {
//                    Log.d(TAG, "Local Data Channel onStateChange: state: ${state()}")
//                    viewModelScope.launch {
//                        _isDataChannelOpen.value = state() == DataChannel.State.OPEN
//                    }
//                }
//
//                override fun onMessage(buffer: DataChannel.Buffer) {
//                    // Send out data, no op on sender side
//                }
//            })
//        }
//    }
//
//    fun sendDataChannelMessage(message: String) {
//        dataChannel?.send(
//            DataChannel.Buffer(
//                ByteBuffer.wrap(message.toByteArray(Charset.defaultCharset())),
//                false
//            )
//        )
//    }
//
//    private fun createSdpOffer() {
//        if (!isValidClient()) {
//            updateConnectionState(ConnectionState.Error("Invalid WebSocket connection"))
//            return
//        }
//
//        val sdpMediaConstraints = MediaConstraints().apply {
//            mandatory.apply {
//                add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
//                add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
//            }
//        }
//
//        localPeer?.createOffer(
//            object : KinesisVideoSdpObserver() {
//                override fun onCreateSuccess(sessionDescription: SessionDescription) {
//                    super.onCreateSuccess(sessionDescription)
//
//                    localPeer?.setLocalDescription(
//                        KinesisVideoSdpObserver(),
//                        sessionDescription
//                    )
//
//                    val sdpOfferMessage = Message.createOfferMessage(
//                        sessionDescription,
//                        clientId ?: return
//                    )
//
//                    client?.sendSdpOffer(sdpOfferMessage)
//                }
//            },
//            sdpMediaConstraints
//        )
//    }
//
//    /**
//     * SDP Answer 생성
//     */
//    private fun createSdpAnswer() {
//        val sdpMediaConstraints = MediaConstraints().apply {
//            mandatory.apply {
//                add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
//                add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
//            }
//        }
//
//        localPeer?.let { peer ->
//            peer.createAnswer(
//                object : KinesisVideoSdpObserver() {
//                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
//                        Log.d(TAG, "Creating answer: success")
//                        super.onCreateSuccess(sessionDescription)
//
//                        peer.setLocalDescription(
//                            KinesisVideoSdpObserver(),
//                            sessionDescription
//                        )
//
//                        val answer = Message.createAnswerMessage(
//                            sessionDescription,
//                            master,
//                            recipientClientId
//                        )
//
//                        client?.sendSdpAnswer(answer)
//
//                        peerConnectionFoundMap[recipientClientId] = peer
//                        handlePendingIceCandidates(recipientClientId)
//                    }
//
//                    override fun onCreateFailure(error: String) {
//                        super.onCreateFailure(error)
//                        Log.e(TAG, "Failed to create SDP answer: $error")
//                        updateConnectionState(ConnectionState.Error("Failed to create answer: $error"))
//                    }
//                },
//                sdpMediaConstraints
//            )
//        }
//    }
//
//    private fun addRemoteStreamToVideoView(stream: MediaStream) {
//        val remoteVideoTrack = stream.videoTracks?.firstOrNull()
//        val remoteAudioTrack = stream.audioTracks?.firstOrNull()
//
//        remoteAudioTrack?.let { audioTrack ->
//            audioTrack.setEnabled(true)
//            Log.d(TAG, "remoteAudioTrack received: State=${audioTrack.state().name}")
//            audioManager.apply {
//                mode = AudioManager.MODE_IN_COMMUNICATION
//                isSpeakerphoneOn = true
//            }
//        }
//
//        remoteVideoTrack?.let { videoTrack ->
//            try {
//                Log.d(TAG, "remoteVideoTrackId=${videoTrack.id()} videoTrackState=${videoTrack.state()}")
//                updateViewSizes()
//                videoTrack.addSink(remoteView)
//            } catch (e: Exception) {
//                Log.e(TAG, "Error in setting remote video view: $e")
//            }
//        } ?: Log.e(TAG, "Error in setting remote track")
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "WebRTC 데이터 채널"
//            val description = "WebRTC 데이터 채널 메시지 알림"
//            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
//                this.description = description
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun handleError(error: String, exception: Exception? = null) {
//        Log.e(TAG, error, exception)
//        updateConnectionState(ConnectionState.Error(error))
//    }
//
//    /**
//     * ICE Candidate 체크 및 추가
//     */
//    private fun checkAndAddIceCandidate(message: Event, iceCandidate: IceCandidate) {
//        val senderClientId = message.senderClientId
//
//        if (!peerConnectionFoundMap.containsKey(senderClientId)) {
//            Log.d(TAG, "SDP exchange is not complete. Ice candidate $iceCandidate added to pending queue")
//
//            // 보류 중인 ICE candidate 큐 생성 또는 가져오기
//            val pendingIceCandidatesQueue = pendingIceCandidatesMap.getOrPut(senderClientId) {
//                LinkedList()
//            }
//            pendingIceCandidatesQueue.add(iceCandidate)
//
//        } else {
//            Log.d(TAG, "Peer connection found, adding ICE candidate")
//            peerConnectionFoundMap[senderClientId]?.let { peer ->
//                peer.addIceCandidate(iceCandidate)
//                Log.d(TAG, "Added ice candidate $iceCandidate")
//            }
//        }
//    }
//
//    /**
//     * 보류 중인 ICE Candidate 처리
//     */
//    private fun handlePendingIceCandidates(clientId: String) {
//        pendingIceCandidatesMap[clientId]?.let { queue ->
//            while (queue.isNotEmpty()) {
//                val iceCandidate = queue.poll()
//                peerConnectionFoundMap[clientId]?.let { peer ->
//                    peer.addIceCandidate(iceCandidate)
//                    Log.d(TAG, "Added pending ice candidate $iceCandidate")
//                }
//            }
//            // 처리 완료 후 큐 제거
//            pendingIceCandidatesMap.remove(clientId)
//        }
//    }
//
//    /**
//     * 피어 연결 해제
//     */
//    private fun disconnectPeer(clientId: String) {
//        peerConnectionFoundMap[clientId]?.dispose()
//        peerConnectionFoundMap.remove(clientId)
//        pendingIceCandidatesMap.remove(clientId)
//    }
//
//
//    private fun initializeVideoComponents() {
//        Log.d(TAG, "Starting video components initialization...")
//
//        if (!isWebRtcInitialized) {
//            Log.e(TAG, "WebRTC not initialized - aborting video components initialization")
//            throw IllegalStateException("WebRTC not initialized")
//        }
//
//        try {
//            val currentConfig = config ?: throw IllegalStateException("WebRTC configuration is missing")
//            Log.d(TAG, "WebRTC configuration loaded successfully")
//
//            // 비디오 캡처러 생성
//            videoCapturer = createVideoCapturer(currentConfig.isFrontCamera)
//            if (videoCapturer == null) {
//                Log.e(TAG, "Failed to create video capturer")
//                throw IllegalStateException("Video capturer creation failed")
//            } else {
//                Log.d(TAG, "Video capturer created successfully")
//            }
//
//            // 비디오 소스 생성
//            videoSource = peerConnectionFactory.createVideoSource(false)
//            if (videoSource == null) {
//                Log.e(TAG, "Failed to create video source")
//                throw IllegalStateException("Video source creation failed")
//            } else {
//                Log.d(TAG, "Video source created successfully")
//            }
//
//            // 서피스 텍스처 헬퍼 생성
//            val surfaceTextureHelper = SurfaceTextureHelper.create(
//                Thread.currentThread().name,
//                rootEglBase?.eglBaseContext
//            )
//            if (surfaceTextureHelper == null) {
//                Log.e(TAG, "Failed to create SurfaceTextureHelper")
//                throw IllegalStateException("SurfaceTextureHelper creation failed")
//            } else {
//                Log.d(TAG, "SurfaceTextureHelper created successfully")
//            }
//
//            // 캡처러 초기화
//            try {
//                videoCapturer?.initialize(
//                    surfaceTextureHelper,
//                    context,
//                    videoSource.capturerObserver
//                )
//                Log.d(TAG, "Video capturer initialized successfully")
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to initialize video capturer", e)
//                throw IllegalStateException("Video capturer initialization failed: ${e.message}")
//            }
//
//            // 로컬 비디오 트랙 생성 및 상태 업데이트
//            localVideoTrack = peerConnectionFactory.createVideoTrack(
//                VIDEO_TRACK_ID,
//                videoSource
//            ).apply {
//                setEnabled(true)
//                _localVideoTrackState.value = this // 로컬 트랙 상태 업데이트
//            }
//            Log.d(TAG, "Local video track created and enabled")
//            Log.e(TAG, "Local video track created: $localVideoTrack")
//
//            // 캡처 시작
//            try {
//                startVideoCapture()
//                Log.d(TAG, "Video capture started successfully")
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to start video capture", e)
//                throw IllegalStateException("Video capture start failed: ${e.message}")
//            }
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to initialize video components", e)
//            updateConnectionState(ConnectionState.Error("Video initialization failed: ${e.message}"))
//            throw e
//        }
//    }
//
//
//    // 원격 비디오 트랙 처리 함수 수정
//    private fun handleRemoteVideoTrack(videoTrack: VideoTrack) {
//        try {
//            remoteVideoTrack?.removeSink(remoteProxyVideoSink)
//            remoteVideoTrack = videoTrack
//            remoteVideoTrack?.setEnabled(true)
//            remoteVideoTrack?.addSink(remoteProxyVideoSink)
//
//            // 원격 트랙 상태 업데이트
//            _remoteVideoTrackState.value = videoTrack
//            _remoteVideoTrackAvailable.value = true
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling remote video track: ${e.message}")
//            _remoteVideoTrackAvailable.value = false
//            _remoteVideoTrackState.value = null
//        }
//    }
//
//
//    // SurfaceViewRenderer 업데이트 함수 수정
//    fun updateLocalVideoView(view: SurfaceViewRenderer) {
//        localView = view
//        viewModelScope.launch {
//            _localVideoTrackState.value?.let { track ->
//                track.addSink(localView)
//                Log.d(TAG, "Local video track added to SurfaceViewRenderer")
//            } ?: Log.e(TAG, "Local video track is null - cannot add to SurfaceViewRenderer")
//        }
//    }
//
//    fun updateRemoteVideoView(view: SurfaceViewRenderer) {
//        remoteView = view
//        viewModelScope.launch {
//            _remoteVideoTrackState.value?.let { track ->
//                track.addSink(remoteView)
//                Log.d(TAG, "Remote video track added to SurfaceViewRenderer")
//            } ?: Log.e(TAG, "Remote video track is null - cannot add to SurfaceViewRenderer")
//        }
//    }
//
//
//
//    private fun startVideoCapture() {
//        try {
//            videoCapturer?.startCapture(
//                VIDEO_SIZE_WIDTH,
//                VIDEO_SIZE_HEIGHT,
//                VIDEO_FPS
//            )
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to start video capture", e)
//            updateConnectionState(ConnectionState.Error("Failed to start camera: ${e.message}"))
//        }
//    }
//
//    fun switchCamera() {
//        viewModelScope.launch {
//            try {
//                val currentConfig = config ?: return@launch
//
//                // 현재 캡처 중지
//                videoCapturer?.stopCapture()
//
//                // 새로운 캡처러 생성 및 초기화
//                videoCapturer = createVideoCapturer(!currentConfig.isFrontCamera)
//
//                val surfaceTextureHelper = SurfaceTextureHelper.create(
//                    Thread.currentThread().name,
//                    rootEglBase?.eglBaseContext
//                )
//
//                videoCapturer?.initialize(
//                    surfaceTextureHelper,
//                    context,
//                    videoSource.capturerObserver
//                )
//
//                // 새로운 캡처 시작
//                startVideoCapture()
//
//                // 설정 업데이트
//                config = currentConfig.copy(isFrontCamera = !currentConfig.isFrontCamera)
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Failed to switch camera", e)
//                updateConnectionState(ConnectionState.Error("카메라 전환 실패: ${e.message}"))
//            }
//        }
//    }
//
//    /**
//     * 오디오 컴포넌트 초기화
//     */
//    private fun initializeAudioComponents() {
//        try {
//            // 오디오 매니저 초기화
//            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//
//            // 현재 오디오 설정 저장
//            originalAudioMode = audioManager.mode
//            originalSpeakerphoneOn = audioManager.isSpeakerphoneOn
//
//            // WebRTC용 오디오 설정
//            audioManager.apply {
//                mode = AudioManager.MODE_IN_COMMUNICATION
//                isSpeakerphoneOn = true
//            }
//
//            // 오디오 소스 및 트랙 생성
//            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
//            localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource).apply {
//                setEnabled(true)
//            }
//
//            Log.d(TAG, "Audio components initialized successfully")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to initialize audio components", e)
//            throw e
//        }
//    }
//
//    /**
//     * 오디오 설정 복원
//     */
//    private fun restoreAudioSettings() {
//        try {
//            if (::audioManager.isInitialized) {
//                audioManager.apply {
//                    mode = originalAudioMode
//                    isSpeakerphoneOn = originalSpeakerphoneOn
//                }
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to restore audio settings", e)
//        }
//    }
//    /**
//     * 모든 WebRTC 관련 리소스를 정리하는 함수
//     */
//
//    fun cleanup() {
//        viewModelScope.launch {
//            try {
//                // 1. 통계 수집 중지
//                printStatsExecutor.shutdownNow()
//
//                // 2. 비디오 관련 리소스 정리
//                cleanupVideoResources()
//
//                // 3. 오디오 관련 리소스 정리
//                cleanupAudioResources()
//
//                // 4. 피어 연결 정리
//                cleanupPeerConnections()
//
//                // 5. WebSocket 연결 종료
//                client?.disconnect()
//                client = null
//
//                // 6. 뷰 리소스 정리
//                cleanupViewResources()
//
//                // 7. 상태 초기화
//                resetStates()
//
//                _localVideoTrackState.value = null
//                _remoteVideoTrackState.value = null
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Cleanup failed", e)
//            }
//        }
//    }
//
//    /**
//     * 비디오 관련 리소스 정리
//     */
//    private fun cleanupVideoResources() {
//        try {
//            // 비디오 캡처 중지
//            videoCapturer?.stopCapture()
//        } catch (e: InterruptedException) {
//            Log.e(TAG, "Failed to stop video capture", e)
//        }
//
//        // 비디오 리소스 해제
//        videoCapturer?.dispose()
//        videoCapturer = null
//
//        if (::videoSource.isInitialized) {
//            videoSource.dispose()
//        }
//
//        if (::localVideoTrack.isInitialized) {
//            localVideoTrack.dispose()
//        }
//    }
//
//    /**
//     * 오디오 관련 리소스 정리
//     */
//    private fun cleanupAudioResources() {
//        if (::localAudioTrack.isInitialized) {
//            localAudioTrack.dispose()
//        }
//        restoreAudioSettings()
//    }
//
//    /**
//     * 피어 연결 관련 리소스 정리
//     */
//    private fun cleanupPeerConnections() {
//        // 데이터 채널 정리
//        dataChannel?.close()
//
//        // 모든 피어 연결 해제
//        peerConnectionFoundMap.forEach { (_, peer) ->
//            peer.dispose()
//        }
//        peerConnectionFoundMap.clear()
//        pendingIceCandidatesMap.clear()
//
//        // 로컬 피어 정리
//        localPeer?.dispose()
//        localPeer = null
//    }
//
//    /**
//     * 뷰 관련 리소스 정리
//     */
//    private fun cleanupViewResources() {
//        localView?.release()
//        localView = null
//
//        remoteView?.release()
//        remoteView = null
//
//        rootEglBase?.release()
//        rootEglBase = null
//    }
//
//    /**
//     * 상태 초기화
//     */
//    private fun resetStates() {
//        _uiState.value = WebRtcUiState()
//        _dataChannelMessage.value = ""
//        _localViewSize.value = Size(0f, 0f)
//        _remoteViewSize.value = Size(0f, 0f)
//    }
//
//
//
//
//    override fun onCleared() {
//        super.onCleared()
//        remoteVideoTrack?.removeSink(remoteProxyVideoSink)
//        remoteVideoTrack = null
//        remoteAudioTrack = null
//        kvsSignalingViewModel.endpointData.removeObserver(observer)
//        kvsSignalingViewModel.webRtcConfig.asLiveData().removeObserver(configObserver)
//        cleanup()
//    }
//}