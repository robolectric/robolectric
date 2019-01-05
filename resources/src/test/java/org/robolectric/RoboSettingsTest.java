package org.robolectric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RoboSettingsTest {

  private String originalMavenRepositoryId;
  private String originalMavenRepositoryUrl;
  private String originalMavenRepositoryUserName;
  private String originalMavenRepositoryPassword;
  private boolean originalUseGlobalScheduler;

  @Before
  public void setUp() {
    originalMavenRepositoryId = RoboSettings.getMavenRepositoryId();
    originalMavenRepositoryUrl = RoboSettings.getMavenRepositoryUrl();
    originalMavenRepositoryUserName = RoboSettings.getMavenRepositoryUserName();
    originalMavenRepositoryPassword = RoboSettings.getMavenRepositoryPassword();
    originalUseGlobalScheduler = RoboSettings.isUseGlobalScheduler();
  }

  @After
  public void tearDown() {
    RoboSettings.setMavenRepositoryId(originalMavenRepositoryId);
    RoboSettings.setMavenRepositoryUrl(originalMavenRepositoryUrl);
    RoboSettings.setMavenRepositoryUserName(originalMavenRepositoryUserName);
    RoboSettings.setMavenRepositoryPassword(originalMavenRepositoryPassword);
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
  public void setMavenRepositoryUserName() {
    RoboSettings.setMavenRepositoryUserName("username");
    assertEquals("username", RoboSettings.getMavenRepositoryUserName());
  }

  @Test
  public void setMavenRepositoryPassword() {
    RoboSettings.setMavenRepositoryPassword("password");
    assertEquals("password", RoboSettings.getMavenRepositoryPassword());
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
