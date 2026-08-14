package org.robolectric.testhelpers

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle

/**
 * Test activity for configuration change tests.
 *
 * This activity tracks lifecycle callbacks and preserves state across configuration changes. It
 * doesn't require any layout resources, making it suitable for unit tests in the runner module.
 */
class ConfigTestActivity : Activity() {
  val transcript: MutableList<String> = mutableListOf()
  var creationCount: Int = 0
  var wasRecreated: Boolean = false
  var savedOriginalInstanceId: Int = 0
  var savedStringState: String? = null
  var savedIntState: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    transcript.add("onCreate")

    if (savedInstanceState != null) {
      wasRecreated = true
      creationCount = savedInstanceState.getInt(KEY_CREATION_COUNT, 0) + 1
      savedOriginalInstanceId = savedInstanceState.getInt(KEY_ORIGINAL_ID, 0)
      savedStringState = savedInstanceState.getString(KEY_STRING_STATE)
      savedIntState = savedInstanceState.getInt(KEY_INT_STATE, 0)
    } else {
      creationCount = 1
    }
  }

  override fun onStart() {
    super.onStart()
    transcript.add("onStart")
  }

  override fun onResume() {
    super.onResume()
    transcript.add("onResume")
  }

  override fun onPause() {
    super.onPause()
    transcript.add("onPause")
  }

  override fun onStop() {
    super.onStop()
    transcript.add("onStop")
  }

  override fun onDestroy() {
    super.onDestroy()
    transcript.add("onDestroy")
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    transcript.add("onSaveInstanceState")
    outState.putInt(KEY_CREATION_COUNT, creationCount)
    outState.putInt(KEY_ORIGINAL_ID, savedOriginalInstanceId)
    savedStringState?.let { outState.putString(KEY_STRING_STATE, it) }
    outState.putInt(KEY_INT_STATE, savedIntState)
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    transcript.add("onConfigurationChanged")
  }

  fun saveOriginalInstanceId(id: Int) {
    savedOriginalInstanceId = id
  }

  fun isPortrait(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
  }

  fun isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  }

  fun getCurrentOrientation(): Int {
    return resources.configuration.orientation
  }

  companion object {
    private const val KEY_CREATION_COUNT = "creation_count"
    private const val KEY_ORIGINAL_ID = "original_id"
    private const val KEY_STRING_STATE = "string_state"
    private const val KEY_INT_STATE = "int_state"
  }
}
