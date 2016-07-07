package org.robolectric.shadows;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(TestRunners.MultiApiWithDefaults.class)

public class ShadowSoundPoolTest {

  @Test
  @Config(sdk = {
          Build.VERSION_CODES.LOLLIPOP,
          Build.VERSION_CODES.LOLLIPOP_MR1,
          Build.VERSION_CODES.M,
  })
  public void shouldCreateSoundPool_Lollipop() {
    SoundPool soundPool = new SoundPool.Builder().build();
    assertThat(soundPool).isNotNull();

    SoundPool.OnLoadCompleteListener listener = mock(SoundPool.OnLoadCompleteListener.class);
    soundPool.setOnLoadCompleteListener(listener);
  }

  @Test
  @Config(sdk = {
          Build.VERSION_CODES.JELLY_BEAN,
          Build.VERSION_CODES.JELLY_BEAN_MR1,
          Build.VERSION_CODES.JELLY_BEAN_MR2,
          Build.VERSION_CODES.KITKAT
  })
  public void shouldCreateSoundPool_JellyBean() {
    SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    assertThat(soundPool).isNotNull();
  }

}
