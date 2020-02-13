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
  public void getPlayCount_noSoundPlayed_zero() {
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(0);
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.FOCUS_COMPLETE)).isEqualTo(0);
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.START_VIDEO_RECORDING))
        .isEqualTo(0);
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
  public void load_once_noException() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
  }

  @Test
  public void load_differentSounds_noException() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
    mediaActionSound.load(MediaActionSound.FOCUS_COMPLETE);
    mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING);
    mediaActionSound.load(MediaActionSound.STOP_VIDEO_RECORDING);
  }

  @Test(expected = IllegalStateException.class)
  public void load_twiceSameSound_exception() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING);
    mediaActionSound.load(MediaActionSound.START_VIDEO_RECORDING);
  }

  @Test
  public void release_notLoaded_noException() {
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.release();
  }

  @Test
  public void getPlayCount_playedOnce() {
    ShadowMediaActionSound.resetPlayCounts();
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(1);
    ShadowMediaActionSound.resetPlayCounts();
  }

  @Test
  public void resetPlayCount_afterPlay_zeroCount() {
    ShadowMediaActionSound.resetPlayCounts();
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.FOCUS_COMPLETE);
    ShadowMediaActionSound.resetPlayCounts();

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.FOCUS_COMPLETE)).isEqualTo(0);
  }

  @Test
  public void play_differentSounds_correctCount() {
    ShadowMediaActionSound.resetPlayCounts();
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
    mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.START_VIDEO_RECORDING))
        .isEqualTo(1);
    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.STOP_VIDEO_RECORDING))
        .isEqualTo(1);
    ShadowMediaActionSound.resetPlayCounts();
  }

  @Test
  public void play_moreThanOnce_correctCount() {
    ShadowMediaActionSound.resetPlayCounts();
    MediaActionSound mediaActionSound = new MediaActionSound();
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK);

    assertThat(ShadowMediaActionSound.getPlayCount(MediaActionSound.SHUTTER_CLICK)).isEqualTo(3);
    ShadowMediaActionSound.resetPlayCounts();
  }
}
