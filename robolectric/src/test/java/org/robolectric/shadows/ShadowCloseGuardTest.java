package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import dalvik.system.CloseGuard;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowCloseGuard}. */
@RunWith(RobolectricTestRunner.class)
public final class ShadowCloseGuardTest {

  @Test
  public void noCloseGuardsOpened_noErrors() {
    assertThat(ShadowCloseGuard.getErrors()).isEmpty();
  }

  @Test
  public void closeGuardsOpened_addedToErrors() {
    CloseGuard closeGuard1 = CloseGuard.get();
    CloseGuard closeGuard2 = CloseGuard.get();

    closeGuard1.open("foo");
    closeGuard2.open("bar");

    assertThat(ShadowCloseGuard.getErrors()).hasSize(2);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  // On < P nothing will be tracked when not enabled. On >= P the String passed in to open() will be
  // tracked when not enabled.
  public void closeGuardsOpened_stackAndTrackingNotEnabled_addedToErrors() {
    CloseGuard.setEnabled(false);
    CloseGuard closeGuard1 = CloseGuard.get();
    CloseGuard closeGuard2 = CloseGuard.get();

    closeGuard1.open("foo");
    closeGuard2.open("bar");

    assertThat(ShadowCloseGuard.getErrors()).hasSize(2);
  }

  @Test
  public void closeGuardsClosed_removedFromErrors() {
    CloseGuard closeGuard1 = CloseGuard.get();
    CloseGuard closeGuard2 = CloseGuard.get();
    closeGuard1.open("foo");
    closeGuard2.open("bar");

    closeGuard1.close();
    closeGuard2.close();

    assertThat(ShadowCloseGuard.getErrors()).isEmpty();
  }

  @Test
  public void closeGuardsOpenedWarnedAndClosed_addedToErrors() {
    CloseGuard closeGuard1 = CloseGuard.get();
    CloseGuard closeGuard2 = CloseGuard.get();
    closeGuard1.open("foo");
    closeGuard2.open("bar");

    closeGuard1.warnIfOpen();
    closeGuard2.warnIfOpen();
    closeGuard1.close();
    closeGuard2.close();

    assertThat(ShadowCloseGuard.getErrors()).hasSize(2);
  }
}
