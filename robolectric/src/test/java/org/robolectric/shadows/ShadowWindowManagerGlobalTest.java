package org.robolectric.shadows;

import android.os.Binder;
import android.os.Looper;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.WindowManagerGlobal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowWindowManagerGlobalTest {

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void toStubAndroidStartup_getWindowSession_shouldReturnWindowSession() throws Exception {
    IWindowSession windowSession = WindowManagerGlobal.getWindowSession();
    assertThat(windowSession).isNotNull();
    assertThat(windowSession.getWindowId(new Binder())).isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void windowId_shouldBeAccessible() throws Exception {
    IWindowSession windowSession = WindowManagerGlobal.getWindowSession();
    assertThat(windowSession).isNotNull();
    IWindowId windowId = windowSession.getWindowId(new Binder());
    assertThat(windowId).isNotNull();
  }
}
