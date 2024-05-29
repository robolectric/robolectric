package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.security.FileIntegrityManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

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
    ((ShadowFileIntegrityManager) Shadow.extract(fileIntegrityManager))
        .setIsApkVeritySupported(false);

    assertThat(fileIntegrityManager.isApkVeritySupported()).isFalse();
  }
}
