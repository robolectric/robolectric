package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR1)
public class ShadowWindowManagerGlobalTest {

  @Test
  public void getWindowSession_shouldReturnNull_toStubAndroidStartup() throws Exception {
    assertThat(ShadowWindowManagerGlobal.getWindowSession()).isNull();
  }

  @Test
  public void getWindowSession_withLooper_shouldReturnNull_toStubAndroidStartup() throws Exception {
    // method not available in JELLY BEAN, sorry :(
    assertThat(ShadowWindowManagerGlobal.getWindowSession(Looper.getMainLooper())).isNull();
  }
}
