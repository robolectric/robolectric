package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.res.Fs;

@RunWith(JUnit4.class)
public class LocalSdkProviderTest {

  @Test
  public void loadSdkJar() throws Exception {
    // TODO: write a proper test. For now, just test with a local dir populated with android all jars
    LocalSdkProvider sdkProvider = new LocalSdkProvider(
        Fs.fromUrl("/tmp/robojars"));
    Collection<Sdk> sdks = sdkProvider.getKnownSdks();
    assertThat(sdks).isNotNull();
  }
}
