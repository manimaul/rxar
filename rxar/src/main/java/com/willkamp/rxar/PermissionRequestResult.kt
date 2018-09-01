package com.willkamp.rxar

import android.support.v4.content.PermissionChecker

data class PermissionRequestResult(
    val permission: String,
    @PermissionChecker.PermissionResult val grantResult: Int
)