package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.system.OsConstants;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowOsConstants}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowOsConstantsTest {

  @Config(minSdk = LOLLIPOP)
  @Test
  public void valuesAreDistinct() throws Exception {
    assertThat(OsConstants.errnoName(OsConstants.EAGAIN)).isEqualTo("EAGAIN");
    assertThat(OsConstants.errnoName(OsConstants.EBADF)).isEqualTo("EBADF");
  }
}
