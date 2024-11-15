//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.navigation.NavController
//import com.amazonaws.services.kinesisvideo.model.ChannelRole
//import com.example.mhnfe.ui.screens.master.KVSSignalingViewModel
//import org.webrtc.SurfaceViewRenderer

package com.example.mhnfe.ui.screens.master

import android.app.NotificationManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.webrtc.SurfaceViewRenderer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.amazonaws.services.kinesisvideo.model.ChannelRole
import org.webrtc.EglBase
//
@Composable
fun WebRtcScreen(
    viewModel: KVSSignalingViewModel,
    navController: NavController,
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val localView by viewModel.localView.collectAsState()
    val remoteView by viewModel.remoteView.collectAsState()


    // EglBase를 컴포저블 레벨에서 생성
    val eglBase = remember { EglBase.create() }

    val connectionEvent by viewModel.connectionEvent.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    // 연결 이벤트 처리
    LaunchedEffect(connectionEvent) {

        when (connectionEvent) {
            ConnectionEvent.ConnectionFailed -> {
                Toast.makeText(context, "Connection error to signaling", Toast.LENGTH_LONG).show()
                navController.navigateUp()  // 화면 종료
                viewModel.onConnectionEventHandled()
            }
            null -> {}
            ConnectionEvent.ConnectionSuccess -> TODO()
        }
    }
    val isViewsInitialized by viewModel.isViewsInitialized.collectAsState()

    // SurfaceViewRenderer 초기화 및 정리
    DisposableEffect(lifecycleOwner) {

        viewModel.initializeSurfaceViews(context, eglBase.eglBaseContext)

        // role이 있을 때만 WebSocket 연결 시작
        if (uiState is WebRTCUiState.Success) {
            val isMaster = (uiState as WebRTCUiState.Success).role == ChannelRole.MASTER
            viewModel.initWsConnection(master = isMaster)
        }

        onDispose {
            localView?.release()
            remoteView?.release()
            eglBase.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // 상단에 비디오 화면 배치
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isViewsInitialized) {
                remoteView?.let { renderer ->
                    AndroidView(
                        factory = { renderer },
                        modifier = Modifier.fillMaxSize().size(120.dp)
                    )
                }

                localView?.let { renderer ->
                    AndroidView(
                        factory = { renderer },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(120.dp)
                            .padding(8.dp)
                    )
                }
            }
        }

        // 하단에 상태 및 컨트롤 UI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            when (uiState) {
                is WebRTCUiState.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                }

                is WebRTCUiState.Success -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Role: ${(uiState as WebRTCUiState.Success).role}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is WebRTCUiState.Error -> {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (uiState as WebRTCUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                else -> { /* 다른 상태 처리 */ }
            }
        }
    }
}

//@Composable
//fun WebRtcScreen(
//    viewModel: KVSSignalingViewModel,
//    navController: NavController,
//    channelName: String,
//    role: ChannelRole,
//) {
//    val remoteVideoTrack by viewModel.remoteVideoTrack.collectAsState()
//    val localVideoTrack by viewModel.localVideoTrack.collectAsState()
//
//
//    // 로컬 뷰와 리모트 뷰 레퍼런스 기억
//    val localViewRef = remember { mutableStateOf<SurfaceViewRenderer?>(null) }
//    val remoteViewRef = remember { mutableStateOf<SurfaceViewRenderer?>(null) }
//
//    // 화면 회전 감지를 위한 Configuration
//    val configuration = LocalConfiguration.current
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        // Remote Video (큰 화면)
//        AndroidView(
//            factory = { context ->
//                SurfaceViewRenderer(context).apply {
//                    init(eglBaseContext, null)
//                    setEnableHardwareScaler(true)
//                    remoteViewRef.value = this
//                }
//            },
//            modifier = Modifier.fillMaxSize(),
//            update = { view ->
//                // 새로운 remote track이 들어오면 sink 업데이트
//                remoteVideoTrack?.addSink(view)
//            }
//        )
//
//        // Local Video (PIP)
//        AndroidView(
//            factory = { context ->
//                SurfaceViewRenderer(context).apply {
//                    init(eglBaseContext, null)
//                    setEnableHardwareScaler(true)
//                    setMirror(true)
//                    localViewRef.value = this
//                }
//            },
//            modifier = Modifier
//                .width(configuration.screenWidthDp.dp * 0.25f)
//                .height(configuration.screenHeightDp.dp * 0.25f)
//                .align(Alignment.TopEnd)
//                .padding(8.dp),
//            update = { view ->
//                // 새로운 local track이 들어오면 sink 업데이트
//                localVideoTrack?.addSink(view)
//            }
//        )
//    }
//
//    // Cleanup
//    DisposableEffect(Unit) {
//        onDispose {
//            localViewRef.value?.release()
//            remoteViewRef.value?.release()
//        }
//    }
//}