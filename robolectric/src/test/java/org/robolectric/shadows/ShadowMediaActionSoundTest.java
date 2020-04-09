package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.media.MediaActionSound;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link org.robolectric.shadows.ShadowMediaActionSound}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.JELLY_BEAN)
public final class ShadowMediaActionSoundTest {
  @Test
  public void getPlayCount_noShutterClickPlayed_zero() {
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(0);
  }

  @Test
  public void getPlayCount_noFocusCompletePlayed_zero() {
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.FOCUS_COMPLETE)).isEqualTo(0);
  }

  @Test
  public void getPlayCount_noStartVideoPlayed_zero() {
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.START_VIDEO_RECORDING))
        .isEqualTo(0);
  }

  @Test
  public void getPlayCount_noStopVideoPlayed_zero() {
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.STOP_VIDEO_RECORDING))
        .isEqualTo(0);
  }

  @Test(expected = RuntimeException.class)
  public void getPlayCount_negativeSoundName_exception() {
    ShadowMediaActionSound.getPlayCount(-1);
  }

  @Test(expected = RuntimeException.class)
  public void getPlayCount_invalidPositiveSoundName_exception() {
    ShadowMediaActionSound.getPlayCount(4);
  }

  @Test
  public void getPlayCount_playedOnce() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(1);
  }

  @Test
  public void getPlayCount_playedDifferentSounds_correctCount() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
    mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.START_VIDEO_RECORDING))
        .isEqualTo(1);
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.STOP_VIDEO_RECORDING))
        .isEqualTo(1);
  }

  @Test
  public void getPlayCount_playedMoreThanOnce_correctCount() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(3);
  }
}
