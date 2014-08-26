package org.robolectric.shadows;

import android.os.Looper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class WindowManagerGlobalTest {

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
