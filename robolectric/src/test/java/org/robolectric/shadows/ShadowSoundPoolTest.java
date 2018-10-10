package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.media.AudioManager;
import android.media.SoundPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
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

    int soundId = soundPool.load(RuntimeEnvironment.application, R.raw.sound, 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(Shadows.shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isTrue();
  }

  @Test
  public void playedSoundsFromPathAreRecorded() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load("/mnt/sdcard/sound.wav", 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(Shadows.shadowOf(soundPool).wasPathPlayed("/mnt/sdcard/sound.wav")).isTrue();
  }

  @Test
  public void playedSoundsAreCleared() {
    SoundPool soundPool = createSoundPool();

    int soundId = soundPool.load(RuntimeEnvironment.application, R.raw.sound, 1);
    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1);

    assertThat(Shadows.shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isTrue();
    Shadows.shadowOf(soundPool).clearPlayed();
    assertThat(Shadows.shadowOf(soundPool).wasResourcePlayed(R.raw.sound)).isFalse();
  }

  private SoundPool createSoundPool() {
    return RuntimeEnvironment.getApiLevel() >= LOLLIPOP
        ? new SoundPool.Builder().build()
        : new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
  }
}
