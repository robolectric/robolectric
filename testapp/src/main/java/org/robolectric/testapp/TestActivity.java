package org.robolectric.testapp;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

/** Test activity that is enabled in the manifest. */
public class TestActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.styles_button_layout);
    Log.d("TestActivity", "onCreate called");

    setupAudioManager();
  }

  private void setupAudioManager() {
    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    if (audioManager != null) {
      Log.d("TestActivity", "Current mode: " + audioManager.getMode());
      // Example to set audio mode
      audioManager.setMode(AudioManager.MODE_RINGTONE);
    } else {
      Log.d("TestActivity", "AudioManager is null");
    }
  }
}
