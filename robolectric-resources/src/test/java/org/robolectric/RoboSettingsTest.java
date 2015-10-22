package org.robolectric;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RoboSettingsTest {

  private String originalMavenRepositoryId;
  private String originalMavenRepositoryUrl;
  private boolean originalUseGlobalScheduler;

  @Before
  public void setUp() {
    originalMavenRepositoryId = RoboSettings.getMavenRepositoryId();
    originalMavenRepositoryUrl = RoboSettings.getMavenRepositoryUrl();
    originalUseGlobalScheduler = RoboSettings.isUseGlobalScheduler();
  }

  @After
  public void tearDown() {
    RoboSettings.setMavenRepositoryId(originalMavenRepositoryId);
    RoboSettings.setMavenRepositoryUrl(originalMavenRepositoryUrl);
    RoboSettings.setUseGlobalScheduler(originalUseGlobalScheduler);
  }

  @Test
  public void getMavenRepositoryId_defaultSonatype() {
    assertEquals("sonatype", RoboSettings.getMavenRepositoryId());
  }

  @Test
  public void setMavenRepositoryId() {
    RoboSettings.setMavenRepositoryId("testRepo");
    assertEquals("testRepo", RoboSettings.getMavenRepositoryId());
  }

  @Test
  public void getMavenRepositoryUrl_defaultSonatype() {
    assertEquals("https://oss.sonatype.org/content/groups/public/", RoboSettings.getMavenRepositoryUrl());
  }

  @Test
  public void setMavenRepositoryUrl() {
    RoboSettings.setMavenRepositoryUrl("http://local");
    assertEquals("http://local", RoboSettings.getMavenRepositoryUrl());
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
