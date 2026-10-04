package org.robolectric.testhelpers

import android.app.Activity
import android.os.Bundle

/** An activity that saves its state across configuration changes. */
class ConfigTestActivity : Activity() {
  var creationCount: Int = 0
  var savedOriginalInstanceId: Int = 0
  var savedStringState: String? = null
  var savedIntState: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (savedInstanceState != null) {
      creationCount = savedInstanceState.getInt(KEY_CREATION_COUNT, 0) + 1
      savedOriginalInstanceId = savedInstanceState.getInt(KEY_ORIGINAL_ID, 0)
      savedStringState = savedInstanceState.getString(KEY_STRING_STATE)
      savedIntState = savedInstanceState.getInt(KEY_INT_STATE, 0)
    } else {
      creationCount = 1
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(KEY_CREATION_COUNT, creationCount)
    outState.putInt(KEY_ORIGINAL_ID, savedOriginalInstanceId)
    savedStringState?.let { outState.putString(KEY_STRING_STATE, it) }
    outState.putInt(KEY_INT_STATE, savedIntState)
  }

  companion object {
    private const val KEY_CREATION_COUNT = "creation_count"
    private const val KEY_ORIGINAL_ID = "original_id"
    private const val KEY_STRING_STATE = "string_state"
    private const val KEY_INT_STATE = "int_state"
  }
}
