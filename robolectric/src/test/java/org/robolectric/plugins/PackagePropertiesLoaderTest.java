package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test for {@link PackagePropertiesLoader}. */
@RunWith(JUnit4.class)
public final class PackagePropertiesLoaderTest {

  @Test
  public void getConfigProperties_whenNoFileExists_cachesNullResult() {
    AtomicInteger callCount = new AtomicInteger();

    PackagePropertiesLoader loader =
        new PackagePropertiesLoader() {
          @Override
          InputStream getResourceAsStream(String resourceName) {
            callCount.incrementAndGet();
            return null;
          }
        };

    assertThat(loader.getConfigProperties("org.example")).isNull();
    assertThat(loader.getConfigProperties("org.example")).isNull();

    assertThat(callCount.get()).isEqualTo(1);
  }
}
