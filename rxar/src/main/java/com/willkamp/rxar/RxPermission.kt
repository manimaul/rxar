package com.willkamp.rxar

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.PermissionChecker
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

  /**
   * Lazily request course and fine location permissions when not already granted.
   */
  fun checkLocationPermissionLazyRequest(context: Context, fragmentManager: FragmentManager): Single<Boolean> {
    return if (PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(context, ACCESS_FINE_LOCATION) &&
        PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(context, ACCESS_COARSE_LOCATION)) {
      Single.just(true)
    } else {
      requestPermissions(fragmentManager, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION).map {
        it.fold(true) { acc, permissionRequestResult ->
          acc && permissionRequestResult.isGranted
        }
      }
    }
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
