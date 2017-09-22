package org.robolectric.shadows.testing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.List;

public class OnMethodTestActivity extends Activity {
  private final List<String> transcript;

  public OnMethodTestActivity(List<String> transcript) {
    this.transcript = transcript;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    transcript.add("onCreate was called with " + savedInstanceState.get("key"));
  }

  @Override
  protected void onStart() {
    transcript.add("onStart was called");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    transcript.add("onRestoreInstanceState was called");
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    transcript.add("onPostCreate was called");
  }

  @Override
  protected void onRestart() {
    transcript.add("onRestart was called");
  }

  @Override
  protected void onResume() {
    transcript.add("onResume was called");
  }

  @Override
  protected void onPostResume() {
    transcript.add("onPostResume was called");
  }

  @Override
  protected void onNewIntent(Intent intent) {
    transcript.add("onNewIntent was called with " + intent);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    transcript.add("onSaveInstanceState was called");
  }

  @Override
  protected void onPause() {
    transcript.add("onPause was called");
  }

  @Override
  protected void onUserLeaveHint() {
    transcript.add("onUserLeaveHint was called");
  }

  @Override
  protected void onStop() {
    transcript.add("onStop was called");
  }

  @Override
  protected void onDestroy() {
    transcript.add("onDestroy was called");
  }
}
