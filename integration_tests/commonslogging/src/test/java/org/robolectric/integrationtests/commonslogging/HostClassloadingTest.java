package org.robolectric.integrationtests.commonslogging;

import static com.google.common.truth.Truth.assertThat;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HostClassloadingTest {
  @Test
  public void logFactoryIsOnTestClasspath() {
    assertThat(LogFactory.getLog(getClass())).isNotNull();
  }
}
