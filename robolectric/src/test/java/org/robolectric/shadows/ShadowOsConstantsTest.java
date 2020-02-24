package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.OsConstantsValues.OPEN_MODE_VALUES;

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

  @Config(minSdk = LOLLIPOP)
  @Test
  public void valuesAreExpected() {
    assertThat(OsConstants.S_IFMT).isEqualTo(OsConstantsValues.S_IFMT_VALUE);
    assertThat(OsConstants.S_IFDIR).isEqualTo(OsConstantsValues.S_IFDIR_VALUE);
    assertThat(OsConstants.S_IFREG).isEqualTo(OsConstantsValues.S_IFREG_VALUE);
    assertThat(OsConstants.S_IFLNK).isEqualTo(OsConstantsValues.S_IFLNK_VALUE);

    assertThat(OsConstants.O_RDONLY).isEqualTo(OPEN_MODE_VALUES.get("O_RDONLY"));
    assertThat(OsConstants.O_WRONLY).isEqualTo(OPEN_MODE_VALUES.get("O_WRONLY"));
    assertThat(OsConstants.O_RDWR).isEqualTo(OPEN_MODE_VALUES.get("O_RDWR"));
    assertThat(OsConstants.O_ACCMODE).isEqualTo(OPEN_MODE_VALUES.get("O_ACCMODE"));
    assertThat(OsConstants.O_CREAT).isEqualTo(OPEN_MODE_VALUES.get("O_CREAT"));
    assertThat(OsConstants.O_EXCL).isEqualTo(OPEN_MODE_VALUES.get("O_EXCL"));
    assertThat(OsConstants.O_TRUNC).isEqualTo(OPEN_MODE_VALUES.get("O_TRUNC"));
    assertThat(OsConstants.O_APPEND).isEqualTo(OPEN_MODE_VALUES.get("O_APPEND"));
  }
}
