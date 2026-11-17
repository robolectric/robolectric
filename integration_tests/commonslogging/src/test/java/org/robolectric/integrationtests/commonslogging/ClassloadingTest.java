package org.robolectric.integrationtests.commonslogging;

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.Init;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Regression coverage for https://github.com/robolectric/robolectric/issues/10590. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.ALL_SDKS)
public class ClassloadingTest {
  @Test
  public void logFactoryIsAvailable() {
    assertThat(LogFactory.getLog(getClass())).isNotNull();
  }

  @Test
  public void libraryCanInitializeLogging() {
    assertThat(LoggingClient.getLogger()).isNotNull();
  }

  @Test
  public void libraryCanInitializeXmlSecurity() {
    LoggingClient.initializeXmlSecurity();
    assertThat(Init.isInitialized()).isTrue();
  }
}
