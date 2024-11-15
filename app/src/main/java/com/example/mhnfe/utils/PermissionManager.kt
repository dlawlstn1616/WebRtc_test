package com.example.mhnfe.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {
    private var isCheckingPermissions = false

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("Permissions", "All permissions are already granted")
        } else {
            val deniedPermissions = permissions.filter { !it.value }
            handleDeniedPermissions(deniedPermissions.keys.toList())
        }
    }

    fun checkAndRequestPermissions() {
        if (isCheckingPermissions) return

        isCheckingPermissions = true

        val ungrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        when {
            ungrantedPermissions.isEmpty() -> {
                Log.d("Permissions", "All permissions are already granted")
            }
            ungrantedPermissions.any { activity.shouldShowRequestPermissionRationale(it) } -> {
                showPermissionRationaleDialog(ungrantedPermissions.toTypedArray())
            }
            else -> {
                permissionLauncher.launch(ungrantedPermissions.toTypedArray())
            }
        }
    }

    private fun showPermissionRationaleDialog(permissions: Array<String>) {
        AlertDialog.Builder(activity)
            .setTitle("권한 필요")
            .setMessage("앱 사용을 위해 다음 권한들이 필요합니다:\n\n" +
                    permissions.joinToString("\n") { "- ${getPermissionName(it)}" })
            .setPositiveButton("권한 요청") { _, _ ->
                permissionLauncher.launch(permissions)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                showPermissionDeniedDialog()
            }
            .show()
    }

    private fun handleDeniedPermissions(deniedPermissions: List<String>) {
        val permanentlyDenied = deniedPermissions.any {
            !activity.shouldShowRequestPermissionRationale(it)
        }

        if (permanentlyDenied) {
            showSettingsDialog()
        } else {
            showPermissionDeniedDialog()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(activity)
            .setTitle("권한 설정")
            .setMessage("일부 권한이 거부되었습니다. 원활한 서비스 이용을 위해 설정에서 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("권한 거부")
            .setMessage("필수 권한이 거부되어 앱 사용이 제한됩니다.\n설정 화면에서 권한을 허용해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("종료") { _, _ ->
                // 앱 종료
                activity.finish()
            }
            .setCancelable(false)  // 뒤로가기 버튼으로 dialog를 닫을 수 없게 설정
            .show()
    }

    private fun checkPermissionsAfterSettings() {
        val ungrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        }
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }

    private fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "카메라 (영상 통화, QR 스캔)"
            Manifest.permission.RECORD_AUDIO -> "마이크 (음성 통화)"
            Manifest.permission.POST_NOTIFICATIONS -> "알림"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "저장소"
            Manifest.permission.MODIFY_AUDIO_SETTINGS -> "오디오 설정"
            else -> permission
        }
    }
}