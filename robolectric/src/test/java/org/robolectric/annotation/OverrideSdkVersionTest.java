package org.robolectric.annotation;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class OverrideSdkVersionTest {
  @Test
  public void whenNotOverridden_shouldUseTargetSdkVersionFromAppManifest() {
    AndroidManifest appManifest = Robolectric.getShadowApplication().getAppManifest();
    assertThat(Build.VERSION.SDK_INT).isEqualTo(appManifest.getTargetSdkVersion());
  }

  @Test
  @Config(reportSdk = Build.VERSION_CODES.FROYO)
  public void whenOverridden_shouldUseSpecifiedVersion() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.FROYO);
  }
}
