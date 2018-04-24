package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.view.WindowManagerGlobal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Robolectric;
import org.robolectric.android.FailureListener;
import org.robolectric.annotation.Config;

@RunWith(JUnit4.class)
public class ShadowWindowManagerGlobalUnitTest {
  @Test
  public void shouldReset() throws Exception {
    assertThat(FailureListener.runTests(DummyTest.class)).isEmpty();
  }

  @Config(sdk = 23)
  public static class DummyTest {
    @Test
    public void first() throws Exception {
      assertThat(WindowManagerGlobal.getInstance().getViewRootNames()).isEmpty();
      Robolectric.setupActivity(Activity.class);
    }

    @Test
    public void second() throws Exception {
      assertThat(WindowManagerGlobal.getInstance().getViewRootNames()).isEmpty();
      Robolectric.setupActivity(Activity.class);
    }
  }

}
