package com.example.mhnfe.ui.screens.cctv

sealed class CameraState{
    object PermissionNotGranted : CameraState()

    object Success : CameraState()
}

