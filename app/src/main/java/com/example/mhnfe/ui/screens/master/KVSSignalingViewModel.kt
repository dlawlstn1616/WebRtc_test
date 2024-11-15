package com.example.mhnfe.ui.screens.master


import android.content.ContentValues.TAG
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesisvideo.AWSKinesisVideoClient
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import com.amazonaws.services.kinesisvideo.model.CreateSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.DescribeSignalingChannelRequest
import com.amazonaws.services.kinesisvideo.model.GetSignalingChannelEndpointRequest
import com.amazonaws.services.kinesisvideo.model.ResourceEndpointListItem
import com.amazonaws.services.kinesisvideo.model.ResourceNotFoundException
import com.amazonaws.services.kinesisvideo.model.SingleMasterChannelEndpointConfiguration
import com.amazonaws.services.kinesisvideosignaling.AWSKinesisVideoSignalingClient
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest
import com.amazonaws.services.kinesisvideosignaling.model.IceServer
import com.amazonaws.services.kinesisvideowebrtcstorage.AWSKinesisVideoWebRTCStorageClient
import com.amazonaws.services.kinesisvideowebrtcstorage.model.JoinStorageSessionRequest
import com.example.mhnfe.MyApplication.Companion.getRegion
import com.example.mhnfe.data.signaling.SignalingListener
import com.example.mhnfe.data.signaling.model.Event
import com.example.mhnfe.data.signaling.model.Message
import com.example.mhnfe.data.signaling.okhttp.SignalingServiceWebSocketClient
import com.example.mhnfe.utils.AwsV4Signer
import com.example.mhnfe.utils.Constants
import com.example.mhnfe.webrtc.KinesisVideoPeerConnection
import com.example.mhnfe.webrtc.KinesisVideoSdpObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.ApplicationContextProvider.getApplicationContext
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.net.URI
import java.util.Date
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


enum class ConnectionEvent {
    ConnectionFailed,
    ConnectionSuccess
    // 필요한 다른 상태들 추가
}





data class WebRtcConfigData(
    val channelArn: String,
    val webrtcEndpoint: String,
    val wssEndpoint: String
)
data class KvsEndpointData(
    val webrtcEndpoint: String,
    val mWssEndpoint: String
)

data class WebRtcConfig(
    val channelName: String,
    val channelArn: String,
    val webrtcEndpoint: String,
    val mWssEndpoint: String,
    val isMaster: Boolean,
    val isFrontCamera: Boolean,
    val isAudioEnabled: Boolean
)


sealed class KvsSignalingState {
    object Initial : KvsSignalingState()
    object Loading : KvsSignalingState()
    data class Success(
        val channelArn: String,
        val streamArn: String,
        val webrtcEndpoint: String,
        val wssEndpoint: String,
        val iceServers: List<IceServer>,
        val isMaster: Boolean
    ) : KvsSignalingState()
    data class Error(val message: String) : KvsSignalingState()
}


class KVSSignalingViewModel : ViewModel() {
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val _localView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val localView: StateFlow<SurfaceViewRenderer?> = _localView

    private val _remoteView = MutableStateFlow<SurfaceViewRenderer?>(null)
    val remoteView: StateFlow<SurfaceViewRenderer?> = _remoteView

    lateinit var videoCapturer: VideoCapturer
    lateinit var localVideoTrack: VideoTrack
    lateinit var videoSource: VideoSource

    private val _isViewsInitialized = MutableStateFlow(false)
    val isViewsInitialized: StateFlow<Boolean> = _isViewsInitialized



    private val _signalingState = MutableStateFlow<KvsSignalingState>(KvsSignalingState.Initial)
    val signalingState: StateFlow<KvsSignalingState> = _signalingState.asStateFlow()

    private val _webRtcConfig = MutableStateFlow<WebRtcConfig?>(null)
    val webRtcConfig: StateFlow<WebRtcConfig?> = _webRtcConfig.asStateFlow()

    private val _endpointData = MutableLiveData<KvsEndpointData>()
    val endpointData: LiveData<KvsEndpointData> get() = _endpointData


    private val _configData = MutableStateFlow<WebRtcConfigData?>(null)
    val configData: StateFlow<WebRtcConfigData?> = _configData

    private val _uiState = MutableStateFlow<WebRTCUiState>(WebRTCUiState.Loading)
    val uiState: StateFlow<WebRTCUiState> = _uiState.asStateFlow()

    //region은 서울로 고정
    private val region = Region.getRegion(Regions.AP_NORTHEAST_2)
    private val regionName = Regions.AP_NORTHEAST_2.getName()

    // 내부 상태를 위한 변수들
    private var channelArn: String? = null
    private val endpointList = mutableListOf<ResourceEndpointListItem>()
    private val iceServerList = mutableListOf<IceServer>()
    private var streamArn: String? = null

    //webRTC관련 변수
    private val clientId = UUID.randomUUID().toString()
    private val printStatsExecutor = Executors.newSingleThreadScheduledExecutor()
    private val peerConnectionFoundMap = mutableMapOf<String, PeerConnection>()
    private val pendingIceCandidatesMap = mutableMapOf<String, Queue<IceCandidate>>()

    private var localPeer: PeerConnection? = null
    private var client: SignalingServiceWebSocketClient? = null

    // WebRTC 연결 관련 변수들
    private var master: Boolean = false  // master/viewer 구분
    private var mCreds: AWSCredentials? = null  // AWS 자격증명
    private var mChannelArn: String? = null  // 채널 ARN
    private var mWssEndpoint: String? = null  // WebSocket 엔드포인트
    private var webrtcEndpoint: String? = null  // WebRTC 엔드포인트
    private var mStreamArn: String? = null  // 스트림 ARN
    private var mRegion: String = Regions.AP_NORTHEAST_2.name  // 리전
    private var gotException: Boolean = false  // 예외 발생 여부
    private var recipientClientId: String? = null  // 수신자 클라이언트 ID
    private var mNotificationId: Int = 0

    // Peer Connection 관련
    private val masterLocalPeer = mutableMapOf<String, PeerConnection>()  // 마스터의 로컬 피어 맵
    private var peerConnectionFactory: PeerConnectionFactory? = null  // 피어 커넥션 팩토리

    // 미디어 관련
    private var isAudioEnabled: Boolean = true  // 오디오 활성화 여부
    private var isFrontCamera: Boolean = true
    private var mClientId = UUID.randomUUID().toString()



    // UI 업데이트를 위한 StateFlow
    private val _localVideoTrackState = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrackState = _localVideoTrackState.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack = _remoteVideoTrack.asStateFlow()




    private fun getCredentialsProvider(): AWSCredentialsProvider {
        return AWSMobileClient.getInstance()
    }

    private val peerIceServers = mutableListOf<PeerConnection.IceServer>()

    fun updateSignalingChannelInfo(
        channelName: String,
        role: ChannelRole,
    ) {
        viewModelScope.launch(Dispatchers.IO) {  // Dispatchers.IO로 변경
            try {
                withContext(Dispatchers.Main) {
                    _uiState.update { WebRTCUiState.Loading }
                }

                Log.d(TAG, "Starting channel update with name: $channelName, role: $role")
                val awsKinesisVideoClient = getAwsKinesisVideoClient()
                Log.d(TAG, "AWS Kinesis Video Client initialized")

                // Describe or Create Signaling Channel
                try {
                    val describeResult = awsKinesisVideoClient.describeSignalingChannel(
                        DescribeSignalingChannelRequest().apply {
                            this.channelName = channelName
                        }
                    )
                    channelArn = describeResult.channelInfo.channelARN
                    Log.d(TAG, "Channel exists - ARN: $channelArn")
                } catch (e: ResourceNotFoundException) {
                    if (role == ChannelRole.MASTER) {
                        val createResult = awsKinesisVideoClient.createSignalingChannel(
                            CreateSignalingChannelRequest().apply {
                                this.channelName = channelName
                            }
                        )
                        channelArn = createResult.channelARN
                        Log.d(TAG, "Created new channel - ARN: $channelArn")
                    } else {
                        Log.e(TAG, "Channel doesn't exist and viewer can't create one")
                        withContext(Dispatchers.Main) {
                            _uiState.update { WebRTCUiState.Error("Signaling Channel $channelName doesn't exist!") }
                        }
                        return@launch
                    }
                }

                // Get Signaling Channel Endpoints
                Log.d(TAG, "Getting channel endpoints for ARN: $channelArn")
                val endpointResult = awsKinesisVideoClient.getSignalingChannelEndpoint(
                    GetSignalingChannelEndpointRequest().apply {
                        this.channelARN = channelArn
                        this.singleMasterChannelEndpointConfiguration =
                            SingleMasterChannelEndpointConfiguration()
                                .withProtocols("WSS", "HTTPS")
                                .withRole(role)
                    }
                )

                endpointResult.resourceEndpointList.forEach { endpoint ->
                    when (endpoint.protocol) {
                        "HTTPS" -> webrtcEndpoint = endpoint.resourceEndpoint
                        "WSS" -> mWssEndpoint = endpoint.resourceEndpoint
                    }
                }

                // 채널 ARN 저장
                mChannelArn = channelArn

                val webrtcEndpoint =
                    endpointResult.resourceEndpointList.find { it.protocol == "HTTPS" }?.resourceEndpoint
                        ?: ""
                var mWssEndpoint =
                    endpointResult.resourceEndpointList.find { it.protocol == "WSS" }?.resourceEndpoint
                        ?: ""

                Log.d(TAG, "WebRTC Endpoint: $webrtcEndpoint")
                Log.d(TAG, "WSS Endpoint: $mWssEndpoint")


                // 스텝 3: ICE 서버 설정 가져오기
                try {
                    val awsKinesisVideoSignalingClient =
                        getAwsKinesisVideoSignalingClient(webrtcEndpoint)
                    val getIceServerConfigResult =
                        awsKinesisVideoSignalingClient.getIceServerConfig(
                            GetIceServerConfigRequest().apply {
                                this.channelARN = channelArn
                                this.clientId = role.name
                            }
                        )

                    // ICE 서버 리스트 저장
                    iceServerList.clear()
                    iceServerList.addAll(getIceServerConfigResult.iceServerList)

                    // ICE 서버 정보를 분리하여 리스트로 저장
                    val userNames = ArrayList<String>()
                    val passwords = ArrayList<String>()
                    val ttls = ArrayList<Int>()
                    val urisList = ArrayList<String>()

                    getIceServerConfigResult.iceServerList.forEach { iceServer ->
                        userNames.add(iceServer.username)
                        passwords.add(iceServer.password)
                        urisList.addAll(iceServer.uris)
                    }

                    // 성공 상태 업데이트
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            WebRTCUiState.Success(
                                channelArn = channelArn ?: "",
                                endpointList = endpointResult.resourceEndpointList,
                                iceServerList = iceServerList,
                                role = role
                            )
                        }

                        // 엔드포인트 데이터 업데이트
                        _endpointData.value = KvsEndpointData(
                            webrtcEndpoint = webrtcEndpoint,
                            mWssEndpoint = mWssEndpoint
                        )

                        Log.d(TAG, "WebRtcViewModel 설정 초기화 완료")
                    }

                    initializeWebRTC(
                        isMaster = role == ChannelRole.MASTER,
                        userNames = userNames,
                        passwords = passwords,
                        urisList = urisList
                    )
//                    initWsConnection(master = role == ChannelRole.MASTER)

                } catch (e: Exception) {
                    Log.e(TAG, "ICE 서버 설정 가져오기 실패", e)
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            WebRTCUiState.Error("ICE 서버 설정 가져오기 실패: ${e.localizedMessage}")
                        }
                    }
                    return@launch
                }

            } catch (e: Exception) {
                Log.e(TAG, "Operation failed", e)
                withContext(Dispatchers.Main) {
                    _uiState.update { WebRTCUiState.Error("Operation failed: ${e.localizedMessage}") }
                }
            }


        }
    }


    private fun initializeWebRTC(
        isMaster: Boolean,
        userNames: List<String>,
        passwords: List<String>,
        urisList: List<String>
    ) {
        applicationContext?.let { context ->
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                    .createInitializationOptions()
            )
        } ?: run {
            Log.e(TAG, "Application context not initialized")
            return
        }
        // Initialize WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
                .createInitializationOptions()
        )


        val rootEglBase = EglBase.create()

        val videoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        val videoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            true
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .createPeerConnectionFactory()

        // Setup video source and capturer
        videoSource = peerConnectionFactory?.createVideoSource(false)!!
        videoCapturer = createVideoCapturer()!!

        if (videoCapturer == null) {
            Log.e(TAG, "Failed to create video capturer")
            return
        }

        val surfaceTextureHelper = SurfaceTextureHelper.create(
            "WebRTC-STH",
            rootEglBase.eglBaseContext
        )

        videoCapturer?.initialize(
            surfaceTextureHelper,
            getApplicationContext(),
            videoSource?.capturerObserver
        )

        localVideoTrack = peerConnectionFactory?.createVideoTrack(
            "VideoTrack",
            videoSource
        )!!

        setupPeerConnection(userNames, passwords, urisList)

        // Start capturing
        videoCapturer?.startCapture(1280, 720, 30)
        localVideoTrack?.setEnabled(true)
    }


    private fun setupPeerConnection(
        userNames: List<String>,
        passwords: List<String>,
        urisList: List<String>
    ) {
        peerIceServers.clear()

        // Add STUN server
        val stun = PeerConnection.IceServer.builder(
            "stun:stun.kinesisvideo.${mRegion}.amazonaws.com:443"
        ).createIceServer()

        peerIceServers.add(stun)

        // Add TURN servers
        if (urisList != null && userNames != null && passwords != null) {
            // 처음 두 개의 TURN 서버만 처리 (turn과 turns)
            val turnServers = urisList.take(2)  // 첫 두 개만 가져오기

            turnServers.forEachIndexed { i, uri ->
                val turnServer = uri.toString()
                val iceServer = PeerConnection.IceServer.builder(
                    turnServer.replace("[", "").replace("]", "")
                )
                    .setUsername(userNames[i])
                    .setPassword(passwords[i])
                    .createIceServer()

                Log.d(TAG, "IceServer details (TURN) = $iceServer")
                peerIceServers.add(iceServer)
            }
        }

    }




    private fun getAwsKinesisVideoClient(): AWSKinesisVideoClient {
        return AWSKinesisVideoClient(
            getCredentialsProvider().credentials
        ).apply {
            setRegion(region)
            setSignerRegionOverride(regionName)
            setServiceNameIntern("kinesisvideo")
        }
    }

    private fun getAwsKinesisVideoSignalingClient(endpoint: String): AWSKinesisVideoSignalingClient {
        return AWSKinesisVideoSignalingClient(
            getCredentialsProvider().credentials
        ).apply {
            setRegion(region)
            setSignerRegionOverride(regionName)
            setServiceNameIntern("kinesisvideo")
            setEndpoint(endpoint)
        }
    }


    private val isCameraFacingFront: Boolean = true

    fun createVideoCapturer(): VideoCapturer? {
        Logging.d(TAG, "Create camera")
        return createCameraCapturer(Camera1Enumerator(false))
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        Logging.d(TAG, "Enumerating cameras")

        for (deviceName in deviceNames) {
            val isDesiredCamera = if (isCameraFacingFront) {
                enumerator.isFrontFacing(deviceName)
            } else {
                enumerator.isBackFacing(deviceName)
            }

            if (isDesiredCamera) {
                Logging.d(TAG, "Camera created")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        return null
    }


    //webRTC코드
    fun initWsConnection(
        master : Boolean,
    ) {

        val localRenderer = _localView.value
        val remoteRenderer = _remoteView.value

        if (localRenderer == null || remoteRenderer == null) {
            Log.e(TAG, "Surface views not initialized")
            return
        }

        if (peerConnectionFactory == null) {
            Log.e(TAG, "PeerConnectionFactory not initialized")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Master endpoint 생성
                val masterEndpoint =
                    "$mWssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=$mChannelArn"
                Log.d("initWsConnection", "mWssEndpoint: $mWssEndpoint")
                Log.d(
                    "initWsConnection",
                    "Constants.CHANNEL_ARN_QUERY_PARAM: ${Constants.CHANNEL_ARN_QUERY_PARAM}"
                )
                Log.d("initWsConnection", "Constants.mChannelArn: $mChannelArn")
                Log.d("initWsConnection", "masterEndpoint: $masterEndpoint")

                // Viewer endpoint 생성
                val viewerEndpoint =
                    "$mWssEndpoint?${Constants.CHANNEL_ARN_QUERY_PARAM}=$mChannelArn&${Constants.CLIENT_ID_QUERY_PARAM}=$mClientId"
                Log.d(
                    "initWsConnection",
                    "Constants.CLIENT_ID_QUERY_PARAM: ${Constants.CLIENT_ID_QUERY_PARAM}"
                )
                Log.d("initWsConnection", "viewerEndpoint: $viewerEndpoint")

                // 크레덴셜 가져오기
                withContext(Dispatchers.Main) {
                    mCreds = getCredentialsProvider().credentials
                }

                // URI 서명
                val signedUri = if (master) {
                    getSignedUri(masterEndpoint)
                } else {
                    getSignedUri(viewerEndpoint)
                }

                if (signedUri == null) {
                    gotException = true
                    return@launch
                }

                val wsHost = signedUri.toString()

                // Step 10. Create Signaling Client Event Listeners
                val signalingListener = object : SignalingListener() {
                    override fun onSdpOffer(offerEvent: Event) {
                        Log.d(TAG, "Received SDP Offer: Setting Remote Description ")

                        val sdp = Event.parseOfferEvent(offerEvent)
                        var peerConnection: PeerConnection? = null

                        if (master) {
                            if (!masterLocalPeer.containsKey(offerEvent.senderClientId)) {
                                createLocalPeerConnection(offerEvent.senderClientId)
                            }
                            peerConnection = masterLocalPeer[offerEvent.senderClientId]
                        } else {
                            peerConnection = localPeer
                        }

                        peerConnection?.let { peer ->
                            peer.setRemoteDescription(
                                KinesisVideoSdpObserver(),
                                SessionDescription(SessionDescription.Type.OFFER, sdp)
                            )
                            recipientClientId = offerEvent.senderClientId
                            Log.d(
                                TAG,
                                "Received SDP offer for client ID: $recipientClientId. Creating answer"
                            )

                            createSdpAnswer(recipientClientId!!)

                            if (master && webrtcEndpoint != null) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Media is being recorded to $mStreamArn",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.i(TAG, "Media is being recorded to $mStreamArn")
                                }
                            }
                        }
                    }

                    override fun onSdpAnswer(answerEvent: Event) {
                        Log.d(TAG, "SDP answer received from signaling")

                        val sdp = Event.parseSdpEvent(answerEvent)
                        val sdpAnswer = SessionDescription(SessionDescription.Type.ANSWER, sdp)

                        localPeer?.let { peer ->
                            peer.setRemoteDescription(object : KinesisVideoSdpObserver() {
                                override fun onCreateFailure(error: String) {
                                    super.onCreateFailure(error)
                                }
                            }, sdpAnswer)
                            Log.d(TAG, "Answer Client ID: ${answerEvent.senderClientId}")
                            peerConnectionFoundMap[answerEvent.senderClientId] = peer
                            // Check if ICE candidates are available in the queue and add the candidate
                            handlePendingIceCandidates(answerEvent.senderClientId)
                        }
                    }

                    override fun onIceCandidate(message: Event) {
                        Log.d(TAG, "Received ICE candidate from remote")
                        val iceCandidate = Event.parseIceCandidate(message)
                        if (iceCandidate != null) {
                            checkAndAddIceCandidate(message, iceCandidate)
                        } else {
                            Log.e(TAG, "Invalid ICE candidate: $message")
                        }
                    }

                    override fun onError(errorMessage: Event) {
                        Log.e(TAG, "Received error message: $errorMessage")
                    }

                    override fun onException(e: Exception) {
                        Log.e(TAG, "Signaling client returned exception: ${e.message}")
                        gotException = true
                    }
                }

                // Step 11. Create SignalingServiceWebSocketClient
                try {
                    client = SignalingServiceWebSocketClient(
                        wsHost,
                        signalingListener,
                        Executors.newFixedThreadPool(10)
                    )

                    Log.d(
                        TAG,
                        "Client connection ${if (client!!.isOpen()) "Successful" else "Failed"}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception with websocket client: $e")
                    gotException = true
                    return@launch
                }

                if (isValidClient()) {
                    Log.d(TAG, "Client connected to Signaling service ${client!!.isOpen()}")

                    if (master) {
                        // If webrtc endpoint is non-null ==> Ingest media was checked
                        if (webrtcEndpoint != null) {
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val storageClient = AWSKinesisVideoWebRTCStorageClient(
                                        getCredentialsProvider().credentials
                                    ).apply {
                                        setRegion(Region.getRegion(mRegion))
                                        setSignerRegionOverride(mRegion)
                                        setServiceNameIntern("kinesisvideo")
                                        setEndpoint(webrtcEndpoint)
                                    }

                                    Log.i(TAG, "mChannelArn: $mChannelArn")
                                    storageClient.joinStorageSession(
                                        JoinStorageSessionRequest().withChannelArn(mChannelArn)
                                    )
                                    Log.i(TAG, "Join storage session request sent!")
                                } catch (ex: Exception) {
                                    Log.e(TAG, "Error sending join storage session request!", ex)
                                }
                            }
                        }
                    } else {
                        Log.d(
                            TAG,
                            "Signaling service is connected: Sending offer as viewer to remote peer"
                        )
                        createSdpOffer()
                    }
                } else {
                    Log.e(TAG, "Error in connecting to signaling service")
                    gotException = true
                }

            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connection failed", e)
                gotException = true
            }
        }
    }

    private fun isValidClient(): Boolean {
        return client != null && client!!.isOpen()
    }

    fun initializeSurfaceViews(context: Context, eglBaseContext: EglBase.Context) {
        val localRenderer = SurfaceViewRenderer(context).apply {
            init(eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(true)
        }
        _localView.value = localRenderer

        val remoteRenderer = SurfaceViewRenderer(context).apply {
            init(eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(false)
        }
        _remoteView.value = remoteRenderer

        _isViewsInitialized.value = true
    }


    private fun createLocalPeerConnection(clientId: String) {
        // RTCConfiguration 설정
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
        }

        // Step 8. Create RTCPeerConnection
        if (master) {
            val masterConnection = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : KinesisVideoPeerConnection() {
                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        val message = createIceCandidateMessage(iceCandidate)
                        Log.d(TAG, "Sending IceCandidate to remote peer $iceCandidate")
                        client?.sendIceCandidate(message)  /* Send to Peer */
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        super.onAddStream(mediaStream)
                        Log.d(TAG, "Adding remote video stream (and audio) to the view")
                        addRemoteStreamToVideoView(mediaStream)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        super.onIceConnectionChange(iceConnectionState)
                        viewModelScope.launch(Dispatchers.Main) {
                            when (iceConnectionState) {
                                PeerConnection.IceConnectionState.FAILED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connection to peer failed!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                PeerConnection.IceConnectionState.CONNECTED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connected to peer!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                    }
                }
            )

            masterConnection?.let { connection ->
                masterLocalPeer[clientId] = connection
                printStatsExecutor.scheduleWithFixedDelay({
                    localPeer?.getStats { rtcStatsReport ->
                        val statsMap = rtcStatsReport.statsMap
                        statsMap.forEach { (key, value) ->
                            Log.d(TAG, "Stats: $key, $value")
                        }
                    }
                }, 0, 10, TimeUnit.SECONDS)

                addStreamToLocalPeer(connection)
            }
        } else {
            localPeer = peerConnectionFactory?.createPeerConnection(
                rtcConfig,
                object : KinesisVideoPeerConnection() {
                    override fun onIceCandidate(iceCandidate: IceCandidate) {
                        super.onIceCandidate(iceCandidate)
                        val message = createIceCandidateMessage(iceCandidate)
                        Log.d(TAG, "Sending IceCandidate to remote peer $iceCandidate")
                        client?.sendIceCandidate(message)  /* Send to Peer */
                    }

                    override fun onAddStream(mediaStream: MediaStream) {
                        super.onAddStream(mediaStream)
                        Log.d(TAG, "Adding remote video stream (and audio) to the view")
                        addRemoteStreamToVideoView(mediaStream)
                    }

                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        super.onIceConnectionChange(iceConnectionState)
                        viewModelScope.launch(Dispatchers.Main) {
                            when (iceConnectionState) {
                                PeerConnection.IceConnectionState.FAILED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connection to peer failed!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                PeerConnection.IceConnectionState.CONNECTED -> {
                                    Toast.makeText(
                                        getApplicationContext(),
                                        "Connected to peer!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                    }
                }
            )

            localPeer?.let { peer ->
                printStatsExecutor.scheduleWithFixedDelay({
                    peer.getStats { rtcStatsReport ->
                        val statsMap = rtcStatsReport.statsMap
                        statsMap.forEach { (key, value) ->
                            Log.d(TAG, "Stats: $key, $value")
                        }
                    }
                }, 0, 10, TimeUnit.SECONDS)

                addStreamToLocalPeer(peer)
            }
        }

    }

    private fun getSignedUri(endpoint: String): URI? {
        // AWS 자격증명 추출
        val accessKey = mCreds?.awsAccessKeyId ?: ""
        val secretKey = mCreds?.awsSecretKey ?: ""

        // 세션 토큰 추출 (Optional 처리를 Kotlin 스타일로 변경)
        val sessionToken = (mCreds as? AWSSessionCredentials)?.sessionToken ?: ""

        // 로그 출력
        Log.e(TAG, "accessKey: $accessKey")
        Log.e(TAG, "secretKey: $secretKey")
        Log.e(TAG, "sessionToken: $sessionToken")

        // 자격증명 유효성 검사
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(
                    getApplicationContext(),
                    "Failed to fetch credentials!",
                    Toast.LENGTH_LONG
                ).show()
            }
            return null
        }

        // URI 서명
        return try {
            AwsV4Signer.sign(
                URI.create(endpoint),
                accessKey,
                secretKey,
                sessionToken,
                URI.create(mWssEndpoint ?: ""),
                mRegion,
                Date().time
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign URI", e)
            null
        }
    }

    private fun createSdpAnswer(clientId: String) {
        val sdpMediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        Log.d(TAG, "createSdpAnswer received: clientId=$clientId")

        val peerConnection = masterLocalPeer[clientId]

        peerConnection?.let { peer ->
            peer.createAnswer(object : KinesisVideoSdpObserver() {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    Log.d(TAG, "Creating answer: success")
                    super.onCreateSuccess(sessionDescription)

                    peer.setLocalDescription(KinesisVideoSdpObserver(), sessionDescription)

                    val answer = Message.createAnswerMessage(
                        sessionDescription = sessionDescription,
                        master = master,
                        recipientClientId = recipientClientId
                    )
                    client?.sendSdpAnswer(answer)

                    recipientClientId?.let { recipient ->
                        peerConnectionFoundMap[recipient] = peer
                        handlePendingIceCandidates(recipient)
                    }
                }

                override fun onCreateFailure(error: String) {
                    super.onCreateFailure(error)

                    // Device is unable to support the requested media format
                    if (error.contains("ERROR_CONTENT")) {
                        Log.e(TAG, "No supported codec is present in the offer!")
                    }
                    gotException = true
                }
            }, sdpMediaConstraints)
        } ?: run {
            Log.e(TAG, "No peerConnection!")
        }
    }

    private fun handlePendingIceCandidates(clientId: String) {
        // Add any pending ICE candidates from the queue for the client ID
        Log.d(TAG, "Pending ice candidates found? ${pendingIceCandidatesMap[clientId]}")

        val pendingIceCandidatesQueueByClientId = pendingIceCandidatesMap[clientId]

        while (pendingIceCandidatesQueueByClientId?.isNotEmpty() == true) {
            val iceCandidate = pendingIceCandidatesQueueByClientId.peek()

            peerConnectionFoundMap[clientId]?.let { peer ->
                iceCandidate?.let { candidate ->
                    val addIce = peer.addIceCandidate(candidate)
                    Log.d(
                        TAG,
                        "Added ice candidate after SDP exchange $candidate ${if (addIce) "Successfully" else "Failed"}"
                    )
                }
            }

            pendingIceCandidatesQueueByClientId.remove()
        }

        // After sending pending ICE candidates, the client ID's peer connection need not be tracked
        pendingIceCandidatesMap.remove(clientId)
    }

    private fun checkAndAddIceCandidate(message: Event, iceCandidate: IceCandidate) {
        // If answer/offer is not received, it means peer connection is not found.
        // Hold the received ICE candidates in the map.
        // Once the peer connection is found, add them directly instead of adding it to the queue.
        val senderClientId = message.senderClientId

        if (!peerConnectionFoundMap.containsKey(senderClientId)) {
            Log.d(
                TAG,
                "SDP exchange is not complete. Ice candidate $iceCandidate added to pending queue"
            )

            // If the entry for the client ID already exists (in case of subsequent ICE candidates),
            // update the queue, otherwise create new queue
            val pendingIceCandidatesQueueByClientId = pendingIceCandidatesMap[senderClientId]
                ?: LinkedList<IceCandidate>().also {
                    pendingIceCandidatesMap[senderClientId] = it
                }

            // Add the candidate to the queue
            pendingIceCandidatesQueueByClientId.add(iceCandidate)
        }
        // This is the case where peer connection is established and ICE candidates are received
        // for the established connection
        else {
            Log.d(TAG, "Peer connection found already")
            // Remote sent us ICE candidates, add to local peer connection
            peerConnectionFoundMap[senderClientId]?.let { peer ->
                val addIce = peer.addIceCandidate(iceCandidate)
                Log.d(
                    TAG,
                    "Added ice candidate $iceCandidate ${if (addIce) "Successfully" else "Failed"}"
                )
            }
        }
    }

    private fun addStreamToLocalPeer(inputPeer: PeerConnection) {
        peerConnectionFactory?.let { factory ->
            val stream = factory.createLocalMediaStream("KvsLocalMediaStream")

            // 직접 VideoTrack 참조 사용
            localVideoTrack?.let { videoTrack ->
                if (!stream.addTrack(videoTrack)) {
                    Log.e(TAG, "Add video track failed")
                }

                stream.videoTracks?.firstOrNull()?.let { firstVideoTrack ->
                    inputPeer.addTrack(firstVideoTrack, listOf(stream.id))
                    // UI 업데이트를 위한 상태 업데이트
                    _localVideoTrackState.value = firstVideoTrack
                }
            }
        }
    }

    private fun addRemoteStreamToVideoView(stream: MediaStream) {
        stream.videoTracks?.firstOrNull()?.let { videoTrack ->
            viewModelScope.launch(Dispatchers.Main) {
                try {
                    Log.d(
                        TAG,
                        "remoteVideoTrackId=${videoTrack.id()} videoTrackState=${videoTrack.state()}"
                    )
                    _remoteVideoTrack.value = videoTrack
                } catch (e: Exception) {
                    Log.e(TAG, "Error in setting remote video view", e)
                }
            }
        } ?: run {
            Log.e(TAG, "Error in setting remote track")
        }
    }

    private fun createIceCandidateMessage(iceCandidate: IceCandidate): Message {
        val sdpMid = iceCandidate.sdpMid
        val sdpMLineIndex = iceCandidate.sdpMLineIndex
        val sdp = iceCandidate.sdp

        // JSON 형식의 메시지 페이로드 생성
        val messagePayload = """
       {
           "candidate":"$sdp",
           "sdpMid":"$sdpMid",
           "sdpMLineIndex":$sdpMLineIndex
       }
   """.trimIndent()

        // master일 경우 빈 문자열, 아닐 경우 mClientId 사용
        val senderClientId = if (master) "" else mClientId

        return Message(
            "ICE_CANDIDATE",
            recipientClientId,
            senderClientId,
            String(
                Base64.encode(
                    messagePayload.toByteArray(),
                    Base64.URL_SAFE or Base64.NO_WRAP
                )
            )
        )
    }
    private fun createSdpOffer() {
        val sdpMediaConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        // 로컬 피어가 없으면 생성
        if (localPeer == null) {
            createLocalPeerConnection(mClientId)
        }

        localPeer?.createOffer(object : KinesisVideoSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                localPeer?.setLocalDescription(KinesisVideoSdpObserver(), sessionDescription)

                val sdpOfferMessage = Message.createOfferMessage(sessionDescription, mClientId)

                if (isValidClient()) {
                    client?.sendSdpOffer(sdpOfferMessage)
                } else {
                    notifySignalingConnectionFailed()
                }
            }
        }, sdpMediaConstraints)
    }

    private val _connectionEvent = MutableStateFlow<ConnectionEvent?>(null)
    val connectionEvent = _connectionEvent.asStateFlow()

    private fun notifySignalingConnectionFailed() {
        viewModelScope.launch {
            _connectionEvent.value = ConnectionEvent.ConnectionFailed
        }
    }
    fun onConnectionEventHandled() {
        _connectionEvent.value = null
    }



}




sealed class WebRTCUiState {
    object Initial : WebRTCUiState()
    object Loading : WebRTCUiState()
    object StorageSessionJoined : WebRTCUiState()
    object Connected : WebRTCUiState() // WebRTC 연결 성공
    data class StreamAdded(val mediaStream: MediaStream) : WebRTCUiState()
    data class MessageReceived(val message: String) : WebRTCUiState()
    data class Success(
        val channelArn: String,
        val endpointList: List<ResourceEndpointListItem>,
        val iceServerList: List<IceServer>,
        val role: ChannelRole
    ) : WebRTCUiState()
    data class Error(val message: String) : WebRTCUiState()
    companion object {
        private const val TAG = "WebRTCViewModel"
    }
}

