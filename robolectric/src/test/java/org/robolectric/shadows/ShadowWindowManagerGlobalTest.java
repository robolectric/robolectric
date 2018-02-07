package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.assertj.core.api.Assertions.assertThat;

import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
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
