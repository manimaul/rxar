package com.willkamp.rxar


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.SingleSubject
import java.security.SecureRandom
import java.util.concurrent.ThreadLocalRandom

private const val MIN_16BIT_INT = 0
private const val MAX_16BIT_INT = 65535
internal fun rand16BitInt(): Int {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    ThreadLocalRandom.current().nextInt(MIN_16BIT_INT, MAX_16BIT_INT + 1)
  } else {
    SecureRandom().nextInt(MAX_16BIT_INT - MIN_16BIT_INT + 1) + MIN_16BIT_INT
  }
}

private val TAG = ActivityResult::class.java.simpleName
private const val KEY_INTENT = "KEY_INTENT"
private const val KEY_OPTIONS = "KEY_OPTIONS"
private const val KEY_REQUEST_CODE = "KEY_REQUEST_CODE"

object RxActivityResult {

  @JvmOverloads
  fun startActivityForResult(fragmentManager: FragmentManager,
                             intent: Intent,
                             options: Bundle? = null): Single<ActivityResult> {
    return Single.defer {
      val broker = ActivityResultBroker()
      val args = Bundle()
      args.putParcelable(KEY_INTENT, intent)
      args.putInt(KEY_REQUEST_CODE, rand16BitInt())
      if (options != null) {
        args.putParcelable(KEY_OPTIONS, options)
      }
      broker.arguments = args

      fragmentManager.beginTransaction()
          ?.add(broker, TAG)
          ?.commit()
      broker.resultSingle
    }.subscribeOn(AndroidSchedulers.mainThread())
  }
}

internal class ActivityResultBroker : Fragment() {

  private val resultSubject = SingleSubject.create<ActivityResult>()

  val resultSingle: Single<ActivityResult>
    get() = resultSubject.hide()

  private val requestCode: Int
    get() = arguments?.getInt(KEY_REQUEST_CODE) ?: 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true
    arguments?.getParcelable<Intent>(KEY_INTENT)?.let { intent ->
      arguments?.getBundle(KEY_OPTIONS)?.let { options ->
        startActivityForResult(intent, requestCode, options)
      } ?: startActivityForResult(intent, requestCode)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == this.requestCode) {
      resultSubject.onSuccess(ActivityResult(resultCode, data))
      fragmentManager?.beginTransaction()
          ?.remove(this)
          ?.commit()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (!resultSubject.hasValue()) {
      resultSubject.onError(IllegalStateException("onActivityResult never occurred"))
    }
  }
}
