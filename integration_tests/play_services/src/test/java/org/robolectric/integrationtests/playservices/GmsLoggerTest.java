package org.robolectric.integrationtests.playservices;

import static com.google.common.truth.Truth.assertThat;

import com.google.android.gms.common.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Test that R8-optimized constructors that reuse parameter slots for incompatible types can be
 * instrumented without producing a VerifyError.
 *
 * <p>The {@link Logger} constructor in play-services-basement is R8-optimized: it overwrites the
 * {@code String[]} parameter slot with a {@code String} before calling {@code super()}. Without the
 * fix in {@code ClassInstrumentor.extractCallToSuperConstructor}, the constructor-splitting
 * transform would produce invalid bytecode.
 */
@RunWith(RobolectricTestRunner.class)
@Config(instrumentedPackages = "com.google.android.gms.common.logging")
public class GmsLoggerTest {
  @Test
  public void loggerCanBeInstantiated() {
    Logger logger = new Logger("TestTag", "category1", "category2");
    assertThat(logger).isNotNull();
    assertThat(logger.getTag()).isEqualTo("TestTag");
  }

  @Test
  public void loggerCanBeInstantiatedWithNoCategories() {
    Logger logger = new Logger("TestTag");
    assertThat(logger).isNotNull();
    assertThat(logger.getTag()).isEqualTo("TestTag");
  }
}
