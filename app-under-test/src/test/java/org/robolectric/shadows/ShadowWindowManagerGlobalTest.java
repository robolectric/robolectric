package org.robolectric.shadows;

import android.os.Build;
import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.MultiApiRobolectricTestRunner;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.JELLY_BEAN_MR1,
    Build.VERSION_CODES.JELLY_BEAN_MR2,
    Build.VERSION_CODES.KITKAT,
    Build.VERSION_CODES.LOLLIPOP })
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
