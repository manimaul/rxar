package com.willkamp.rxar

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.support.v4.content.PermissionChecker

data class PermissionRequestResult(
    val permission: String,
    @PermissionChecker.PermissionResult val grantResult: Int
) {
  val isGranted
    get() =
      grantResult == PERMISSION_GRANTED
}