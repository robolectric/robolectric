package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.shadows.ShadowApplication;

@RunWith(TestRunners.SelfTest.class)
public class OverrideSdkVersionTest {
  @Test
  public void whenNotOverridden_shouldUseTargetSdkVersionFromAppManifest() {
    AndroidManifest appManifest = ShadowApplication.getInstance().getAppManifest();
    assertThat(Build.VERSION.SDK_INT).isEqualTo(appManifest.getTargetSdkVersion());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void whenOverridden_shouldUseSpecifiedVersion() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.JELLY_BEAN_MR1);
  }
}
