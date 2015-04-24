package org.robolectric.annotation;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowApplication;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
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
