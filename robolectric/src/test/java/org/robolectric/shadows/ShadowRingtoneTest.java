package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.media.Ringtone;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ShadowRingtone}. */
@RunWith(AndroidJUnit4.class)
public class ShadowRingtoneTest {
  private Ringtone ringtone;

  @Before
  public void setup() throws Exception {
    ringtone = ShadowRingtone.create();
  }

  @Test
  public void getPlayCount_playNotCalled_shouldReturnZero() {
    assertThat(shadowOf(ringtone).getPlayCount()).isEqualTo(0);
  }

  @Test
  public void getPlayCount_playCalledOnce_shouldReturnOne() {
    ringtone.play();

    assertThat(shadowOf(ringtone).getPlayCount()).isEqualTo(1);
  }

  @Test
  public void getPlayCount_playCalledThrice_shouldReturnThree() {
    ringtone.play();
    ringtone.play();
    ringtone.play();

    assertThat(shadowOf(ringtone).getPlayCount()).isEqualTo(3);
  }
}
