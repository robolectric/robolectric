package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.media.AudioManager;
import android.media.SoundPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
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

}
