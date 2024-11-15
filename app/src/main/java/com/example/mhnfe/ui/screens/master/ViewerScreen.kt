package com.example.mhnfe.ui.screens.master


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ViewerScreen(
//    onBackClick: () -> Unit,
//    viewModel: ViewerViewModel = hiltViewModel()
//) {
//    val viewerState by viewModel.viewerState.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("실시간 모니터링") },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, "Back")
//                    }
//                },
//                actions = {
//                    when (viewerState) {
//                        is ViewerState.Watching -> {
//                            Box(
//                                modifier = Modifier
//                                    .padding(end = 16.dp)
//                                    .size(12.dp)
//                                    .background(Color.Green, CircleShape)
//                            )
//                        }
//                        else -> {}
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // 영상 표시 영역
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//                    .background(Color.Black)
//            ) {
//                when (viewerState) {
//                    is ViewerState.Connecting -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                CircularProgressIndicator(color = Color.White)
//                                Text(
//                                    "스트리밍 연결 중...",
//                                    color = Color.White
//                                )
//                            }
//                        }
//                    }
//                    is ViewerState.Error -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = (viewerState as ViewerState.Error).message,
//                                color = Color.Red
//                            )
//                        }
//                    }
//                    is ViewerState.Ready -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                "스트리밍 시작을 기다리는 중...",
//                                color = Color.White
//                            )
//                        }
//                    }
//                    else -> {}
//                }
//            }
//
//            // 컨트롤 패널
//            ViewerControls(
//                viewerState = viewerState,
//                onStartClick = { viewModel.startWatching() },
//                onStopClick = { viewModel.stopWatching() }
//            )
//        }
//    }
//}
//
//
//@Composable
//private fun ViewerControls(
//    viewerState: ViewerState,
//    onStartClick: () -> Unit,
//    onStopClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Text(
//                text = when (viewerState) {
//                    is ViewerState.Ready -> "준비됨"
//                    is ViewerState.Connecting -> "연결 중..."
//                    is ViewerState.Watching -> "시청 중"
//                    is ViewerState.Error -> "오류 발생"
//                },
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                when (viewerState) {
//                    is ViewerState.Ready -> {
//                        Button(
//                            onClick = onStartClick,
//                            modifier = Modifier.weight(1f),
//                        ) {
//                            Text("시청 시작")
//                        }
//                    }
//                    is ViewerState.Watching -> {
//                        Button(
//                            onClick = onStopClick,
//                            modifier = Modifier.weight(1f),
//                        ) {
//                            Text("시청 중지")
//                        }
//                    }
//                    else -> {
//                        Button(
//                            onClick = { },
//                            modifier = Modifier.weight(1f),
//                            enabled = false
//                        ) {
//                            Text("처리 중...")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//@Preview(showBackground = true)
//@Composable
//private fun Preview(){
//    val navController = rememberNavController()
//    val viewModel:ViewerViewModel = hiltViewModel()
//
//    ViewerScreen(onBackClick = {}, viewModel = viewModel)
//
//}

//@Composable
//fun ViewerScreen(
//    modifier: Modifier = Modifier,
//    viewModel: KVSViewModel = viewModel(),
//    onNavigateBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val activity = context.findActivity()
//    val isConnected by viewModel.isConnected.collectAsState()
//
//    // 가로 모드 설정
//    DisposableEffect(Unit) {
//        val originalOrientation = activity?.requestedOrientation
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//
//        onDispose {
//            if (originalOrientation != null) {
//                activity?.requestedOrientation = originalOrientation
//            }
//        }
//    }
//
//    // KVS 초기화
//    LaunchedEffect(Unit) {
//        viewModel.initializeKVS(context)
//    }
//
//    Box(
//        modifier = modifier.fillMaxSize()
//    ) {
//        // 스트림 뷰어
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = { context ->
//                SurfaceView(context).apply {
//                    layoutParams = ViewGroup.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT
//                    )
//                    holder.addCallback(object : SurfaceHolder.Callback {
//                        override fun surfaceCreated(holder: SurfaceHolder) {
//                            viewModel.startViewing(
//                                streamName = "meong-ha-nyang_KVS",  // CCTV 스트림 이름
//                            )
//                        }
//
//                        override fun surfaceChanged(
//                            holder: SurfaceHolder,
//                            format: Int,
//                            width: Int,
//                            height: Int
//                        ) {}
//
//                        override fun surfaceDestroyed(holder: SurfaceHolder) {
//                            viewModel.stopViewing()
//                        }
//                    })
//                }
//            }
//        )
//
//        // 연결 상태 표시
//        if (isConnected) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f)),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    CircularProgressIndicator(color = Color.White)
//                    Text(
//                        "연결중...",
//                        color = Color.White,
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                }
//            }
//        }
//
//        // 컨트롤 버튼
//        Row(
//            modifier = Modifier
//                .align(Alignment.TopCenter)
//                .fillMaxWidth()
//                .padding(70.dp, 20.dp),
//            horizontalArrangement = Arrangement.End,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Button(
//                onClick = {
//                    viewModel.stopViewing()
//                    onNavigateBack()
//                }
//            ) {
//                Text("연결 종료")
//            }
//        }
//    }
//}