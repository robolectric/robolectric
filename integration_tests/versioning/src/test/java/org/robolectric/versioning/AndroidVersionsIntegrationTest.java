package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.inject.Injector;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;

/**
 * Check versions information aligns with runtime information. Primarily, selected SDK with
 * internally detected version number.
 */
@RunWith(JUnit4.class)
public final class AndroidVersionsIntegrationTest {

  /**
   * Verify that the SDK metadata calculated by AndroidVersions matches that given by the
   * SDkProvider.
   *
   * <p>This is important to ensure consistency between annotation validation and runtime and avoid
   * surprising behavior.
   */
  @Test
  public void sdksMatchSdkProvider() throws IOException {
    Injector injector =
        new Injector.Builder().bind(Properties.class, System.getProperties()).build();
    SdkProvider provider = injector.getInstance(SdkProvider.class);
    for (Sdk sdk : provider.getSdks()) {
      AndroidRelease releaseFromVersions =
          AndroidVersions.computeReleaseVersion(new JarFile(sdk.getJarPath().toFile()));
      assertThat(releaseFromVersions.getSdkInt()).isEqualTo(sdk.getApiLevel());
    }
  }
}
