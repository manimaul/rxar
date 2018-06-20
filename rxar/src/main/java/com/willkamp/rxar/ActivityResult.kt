package com.willkamp.rxar

import android.app.Activity
import android.content.Intent

data class ActivityResult(
    val resultCode: Int,
    val data: Intent?
) {

  val isResultOk
    get() = resultCode == Activity.RESULT_OK
}