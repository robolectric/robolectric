package org.robolectric.shadows;

import android.app.Activity;
import android.view.WindowManagerGlobal;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.android.FailureListener;

import static org.assertj.core.api.Assertions.assertThat;

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
