package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.media.AudioManager;
import android.media.SoundPool;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSoundPool.Playback;

@RunWith(AndroidJUnit4.class)
public class ShadowSoundPoolTest {

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldCreateSoundPool_Lollipop() {
    SoundPool soundPool = new SoundPool.Builder().build();
    assertThat(soundPool).isNotNull();

    SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
    soundPool.setOnLoadCompleteListener(listener);
  }

  @Test
  @Config(maxSdk = JELLY_BEAN_MR2)
  public void shouldCreateSoundPool_JellyBean() {
    SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    assertThat(soundPool).isNotNull();
  }

  @Test
  public void playedSoundsFromResourcesAreRecorded() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load(ApplicationProvider.getApplicationContext(), R.raw.sound, 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isTrue();
  }

  @Test
  public void playedSoundsFromResourcesAreCollected() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load(ApplicationProvider.getApplicationContext(), R.raw.sound, 1);
    soundPool.play(soundId, 1.0f, 0f, 0, 0, 0.5f);
    soundPool.play(soundId, 0f, 1.0f, 1, 0, 2.0f);

    assertThat(shadowOf(soundPool).getResourcePlaybacks(R.raw.sound))
        .containsExactly(
            new Playback(soundId, 1.0f, 0f, 0, 0, 0.5f),
            new Playback(soundId, 0f, 1.0f, 1, 0, 2.0f))
        .inOrder();
  }

  @Test
  public void playedSoundsFromPathAreRecorded() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load("/mnt/sdcard/sound.wav", 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(shadowOf(soundPool).wasPathPlayed("/mnt/sdcard/sound.wav")).isTrue();
  }

  @Test
  public void playedSoundsFromPathAreCollected() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load("/mnt/sdcard/sound.wav", 1);
    soundPool.play(soundId, 0f, 1.0f, 1, 0, 2.0f);
    soundPool.play(soundId, 1.0f, 0f, 0, 0, 0.5f);

    assertThat(shadowOf(soundPool).getPathPlaybacks("/mnt/sdcard/sound.wav"))
        .containsExactly(
            new Playback(soundId, 0f, 1.0f, 1, 0, 2.0f),
            new Playback(soundId, 1.0f, 0f, 0, 0, 0.5f))
        .inOrder();
  }

  @Test
  public void notifyPathLoaded_notifiesListener() {
    SoundPool soundPool = createSoundPool();
    SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
    soundPool.setOnLoadCompleteListener(listener);

    int soundId = soundPool.load("/mnt/sdcard/sound.wav", 1);
    shadowOf(soundPool).notifyPathLoaded("/mnt/sdcard/sound.wav", true);

    verify(listener).onLoadComplete(soundPool, soundId, 0);
  }

  @Test
  public void notifyResourceLoaded_notifiesListener() {
    SoundPool soundPool = createSoundPool();
    SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
    soundPool.setOnLoadCompleteListener(listener);

    int soundId = soundPool.load(ApplicationProvider.getApplicationContext(), R.raw.sound, 1);
    shadowOf(soundPool).notifyResourceLoaded(R.raw.sound, true);

    verify(listener).onLoadComplete(soundPool, soundId, 0);
  }

  @Test
  public void notifyPathLoaded_notifiesFailure() {
    SoundPool soundPool = createSoundPool();
    SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
    soundPool.setOnLoadCompleteListener(listener);

    int soundId = soundPool.load("/mnt/sdcard/sound.wav", 1);
    shadowOf(soundPool).notifyPathLoaded("/mnt/sdcard/sound.wav", false);

    verify(listener).onLoadComplete(soundPool, soundId, 1);
  }

  @Test
  public void notifyResourceLoaded_doNotFailWithoutListener() {
    SoundPool soundPool = createSoundPool();

    soundPool.load("/mnt/sdcard/sound.wav", 1);
    shadowOf(soundPool).notifyPathLoaded("/mnt/sdcard/sound.wav", false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void notifyPathLoaded_failIfLoadWasntCalled() {
    SoundPool soundPool = createSoundPool();

    shadowOf(soundPool).notifyPathLoaded("no.mp3", true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void notifyResourceLoaded_failIfLoadWasntCalled() {
    SoundPool soundPool = createSoundPool();

    shadowOf(soundPool).notifyResourceLoaded(123, true);
  }

  @Test
  public void playedSoundsAreCleared() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load(ApplicationProvider.getApplicationContext(), R.raw.sound, 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isTrue();
    shadowOf(soundPool).clearPlayed();
    assertThat(shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isFalse();
  }

  private SoundPool createSoundPool() {
    return RuntimeEnvironment.getApiLevel() >= LOLLIPOP
        ? new SoundPool.Builder().build()
        : new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
  }
}
