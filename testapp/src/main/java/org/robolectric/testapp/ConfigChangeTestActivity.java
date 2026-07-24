package org.robolectric.testapp;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

/**
 * Test activity for configuration change testing.
 *
 * <p>Tracks lifecycle callbacks and state preservation across configuration changes like device
 * rotation, theme changes, and locale changes.
 */
public class ConfigChangeTestActivity extends Activity {

  /** Transcript of lifecycle method calls for verification in tests. */
  public final List<String> transcript = new ArrayList<>();

  /** Counter for number of times activity has been created. */
  public int creationCount = 0;

  /** Stored instance ID from previous instance (for recreation verification). */
  public int savedOriginalInstanceId = 0;

  /** Whether this instance was recreated from a previous instance. */
  public boolean wasRecreated = false;

  /** Saved string state for testing state preservation. */
  public String savedStringState = null;

  /** Saved integer state for testing state preservation. */
  public int savedIntState = 0;

  private static final String KEY_CREATION_COUNT = "creation_count";
  private static final String KEY_ORIGINAL_ID = "original_id";
  private static final String KEY_STRING_STATE = "string_state";
  private static final String KEY_INT_STATE = "int_state";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    transcript.add("onCreate");

    int layoutId = getResources().getIdentifier("activity_config_test", "layout", getPackageName());
    setContentView(layoutId);

    if (savedInstanceState != null) {
      wasRecreated = true;
      creationCount = savedInstanceState.getInt(KEY_CREATION_COUNT, 0) + 1;
      savedOriginalInstanceId = savedInstanceState.getInt(KEY_ORIGINAL_ID, 0);
      savedStringState = savedInstanceState.getString(KEY_STRING_STATE);
      savedIntState = savedInstanceState.getInt(KEY_INT_STATE, 0);
    } else {
      creationCount = 1;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    transcript.add("onStart");
  }

  @Override
  protected void onResume() {
    super.onResume();
    transcript.add("onResume");
  }

  @Override
  protected void onPause() {
    super.onPause();
    transcript.add("onPause");
  }

  @Override
  protected void onStop() {
    super.onStop();
    transcript.add("onStop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    transcript.add("onDestroy");
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    transcript.add("onSaveInstanceState");

    outState.putInt(KEY_CREATION_COUNT, creationCount);
    outState.putInt(KEY_ORIGINAL_ID, savedOriginalInstanceId);
    outState.putString(KEY_STRING_STATE, savedStringState);
    outState.putInt(KEY_INT_STATE, savedIntState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    transcript.add("onRestoreInstanceState");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    transcript.add("onConfigurationChanged");
  }

  /** Save the original instance ID for verification in tests. */
  public void saveOriginalInstanceId(int instanceId) {
    this.savedOriginalInstanceId = instanceId;
  }

  /** Save custom state for testing state preservation. */
  public void saveCustomState(String stringValue, int intValue) {
    this.savedStringState = stringValue;
    this.savedIntState = intValue;
  }

  /** Get the current orientation (portrait or landscape). */
  public int getCurrentOrientation() {
    return getResources().getConfiguration().orientation;
  }

  /** Check if current orientation is landscape. */
  public boolean isLandscape() {
    return getCurrentOrientation() == Configuration.ORIENTATION_LANDSCAPE;
  }

  /** Check if current orientation is portrait. */
  public boolean isPortrait() {
    return getCurrentOrientation() == Configuration.ORIENTATION_PORTRAIT;
  }
}
