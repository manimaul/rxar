package com.willkamp.rxar

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.willkamp.rxar.RxActivityResult.startActivityForResult
import io.reactivex.rxkotlin.subscribeBy
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RxActivityResultTest {


  private lateinit var instrumentation: Instrumentation
  private lateinit var activityStack: ActivityStack
  private val application: Application
    get() = InstrumentationRegistry.getTargetContext().applicationContext as Application
  @get:Rule
  val activityRule = ActivityTestRule<MainActivity>(MainActivity::class.java, false, false)

  @Before
  fun setup() {
    instrumentation = InstrumentationRegistry.getInstrumentation()
    activityStack = ActivityStack()
    application.registerActivityLifecycleCallbacks(activityStack)
    activityRule.launchActivity(null)
  }

  @Test
  @Throws(Exception::class)
  fun sanityCheck() {
    assertEquals(1, activityStack.count)
    assertEquals(activityRule.activity, activityStack.currentActivity)
  }

  @Test
  @Throws(Exception::class)
  fun rxActivityResult() {
    val otherActivityIntent = Intent(activityRule.activity, OtherActivity::class.java)

    var activityResult: ActivityResult? = null
    var error: Throwable? = null
    startActivityForResult(activityRule.activity.supportFragmentManager, otherActivityIntent).subscribeBy(
        onSuccess = {
          activityResult = it
        },
        onError = {
          error = it
        }
    )
    activityStack.waitForActivityCount(2)
    assertEquals(OtherActivity::class.java, activityStack.currentActivity?.javaClass)

    val result = Intent()
    result.putExtra("key", "value")
    activityStack.currentActivity?.setResult(Activity.RESULT_OK, result)
    activityStack.currentActivity?.finish()

    activityStack.waitForActivityCount(1)

    assertNull(error)
    assertTrue(activityResult?.isResultOk ?: false)
    assertEquals(Activity.RESULT_OK, activityResult?.resultCode)
    assertEquals("value", activityResult?.data?.getStringExtra("key"))
  }
}
