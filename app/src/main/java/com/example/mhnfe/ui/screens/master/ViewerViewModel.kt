package com.example.mhnfe.ui.screens.master

//@HiltViewModel
//class ViewerViewModel @Inject constructor(
//    application: Application
//) : AndroidViewModel(application) {
//    private val _viewerState = MutableStateFlow<ViewerState>(ViewerState.Ready)
//    val viewerState: StateFlow<ViewerState> = _viewerState.asStateFlow()
//
//    fun startWatching() {
//        viewModelScope.launch {
//            _viewerState.value = ViewerState.Connecting
//            // TODO: WebRTC 연결 구현
//            _viewerState.value = ViewerState.Watching
//        }
//    }
//
//    fun stopWatching() {
//        viewModelScope.launch {
//            // TODO: WebRTC 연결 종료 구현
//            _viewerState.value = ViewerState.Ready
//        }
//    }
//}
//
//sealed class ViewerState {
//    object Ready : ViewerState()
//    object Connecting : ViewerState()
//    object Watching : ViewerState()
//    data class Error(val message: String) : ViewerState()
//}