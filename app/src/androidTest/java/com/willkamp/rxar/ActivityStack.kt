package com.willkamp.rxar

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityStack : Application.ActivityLifecycleCallbacks {

  private var activityStack: Stack<Activity> = Stack()
  private val countVariable = Variable(0)

  val currentActivity: Activity?
    get() = if (activityStack.empty()) null else activityStack.peek()

  val count: Int
    get() = countVariable.value

  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityStarted(activity: Activity) {
  }

  override fun onActivityDestroyed(activity: Activity) {
    activityStack.remove(activity)
    countVariable.value = activityStack.count()
  }

  override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle?) {
  }

  override fun onActivityStopped(activity: Activity) {
  }

  override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    activityStack.push(activity)
    countVariable.value = activityStack.count()
  }

  fun waitForActivityCount(count: Int) {
    countVariable.valueObservable.takeUntil {
      it == count
    }.timeout(5, TimeUnit.SECONDS, Schedulers.io()).doOnError {
      Assert.fail("timed out waiting for activity count: $count")
    }.blockingLast()
  }
}

class Variable<T>(private var internalValue: T) {

  private val valueSubject: BehaviorSubject<T> = BehaviorSubject.create()

  var value: T
    set(value) = {
      internalValue = value
      valueSubject.onNext(value)
    }()
    get() = internalValue

  val valueObservable: Observable<T>
    get() = valueSubject

}