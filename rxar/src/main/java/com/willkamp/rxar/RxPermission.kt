package com.willkamp.rxar

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.SingleSubject

object RxPermission {


  /**
   * Request runtime permissions and get an Observable<List<[PermissionRequestResult]>> result.
   *
   * @param permissions The permissions to request
   */
  fun requestPermissions(fragmentManager: FragmentManager,
                         vararg permissions: String): Single<List<PermissionRequestResult>> {
    return Single.defer {
      val broker = PermissionRequestBroker()
      broker.arguments = Bundle()
      broker.arguments?.putStringArray(PermissionRequestBroker.KEY_PERMISSIONS, permissions)
      broker.arguments?.putInt(PermissionRequestBroker.KEY_REQUEST_CODE, rand16BitInt())
      fragmentManager.beginTransaction()
          .add(broker, PermissionRequestBroker.TAG)
          .commit()
      broker.resultObservable
    }.subscribeOn(AndroidSchedulers.mainThread())
  }
}

/**
 * Headless retain fragment for brokering an Observable result from requesting permissions.
 */
internal class PermissionRequestBroker : Fragment() {
  private val resultSubject = SingleSubject.create<List<PermissionRequestResult>>()
  val resultObservable: Single<List<PermissionRequestResult>>
    get() = resultSubject

  companion object {
    internal val TAG = PermissionRequestBroker::class.java.simpleName
    internal const val KEY_PERMISSIONS = "KEY_PERMISSIONS"
    internal const val KEY_REQUEST_CODE = "KEY_REQUEST_CODE"
  }

  private val requestCode: Int
    get() = arguments?.getInt(KEY_REQUEST_CODE) ?: 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    retainInstance = true
    arguments?.getStringArray(KEY_PERMISSIONS)?.let {
      requestPermissions(it, requestCode)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == this.requestCode) {
      resultSubject.onSuccess(grantResults.zip(permissions).map { PermissionRequestResult(it.second, it.first) })
      requireFragmentManager()
          .beginTransaction()
          .remove(this)
          .commit()
    }
  }
}
