package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.security.FileIntegrityManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public final class ShadowFileIntegrityManagerTest {

  private FileIntegrityManager fileIntegrityManager;

  @Before
  public void setUp() {
    fileIntegrityManager =
        ApplicationProvider.getApplicationContext().getSystemService(FileIntegrityManager.class);
  }

  @Test
  public void isApkVeritySupported_returnsTrue() {
    assertThat(fileIntegrityManager.isApkVeritySupported()).isTrue();
  }

  @Test
  public void isApkVeritySupported_setFalse_returnsFalse() {
    shadowOf(fileIntegrityManager).setIsApkVeritySupported(false);

    assertThat(fileIntegrityManager.isApkVeritySupported()).isFalse();
  }
}
