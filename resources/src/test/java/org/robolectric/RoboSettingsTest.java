package org.robolectric;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RoboSettingsTest {

  private boolean originalUseGlobalScheduler;

  @Before
  public void setUp() {
    originalUseGlobalScheduler = RoboSettings.isUseGlobalScheduler();
  }

  @After
  public void tearDown() {
    RoboSettings.setUseGlobalScheduler(originalUseGlobalScheduler);
  }

  @Test
  public void isUseGlobalScheduler_defaultFalse() {
    assertFalse(RoboSettings.isUseGlobalScheduler());
  }

  @Test
  public void setUseGlobalScheduler() {
    RoboSettings.setUseGlobalScheduler(true);
    assertTrue(RoboSettings.isUseGlobalScheduler());
  }
}
