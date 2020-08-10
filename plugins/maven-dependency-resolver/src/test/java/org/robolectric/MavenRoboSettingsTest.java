package org.robolectric;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MavenRoboSettingsTest {

  private String originalMavenRepositoryId;
  private String originalMavenRepositoryUrl;
  private String originalMavenRepositoryUserName;
  private String originalMavenRepositoryPassword;

  @Before
  public void setUp() {
    originalMavenRepositoryId = MavenRoboSettings.getMavenRepositoryId();
    originalMavenRepositoryUrl = MavenRoboSettings.getMavenRepositoryUrl();
    originalMavenRepositoryUserName = MavenRoboSettings.getMavenRepositoryUserName();
    originalMavenRepositoryPassword = MavenRoboSettings.getMavenRepositoryPassword();
  }

  @After
  public void tearDown() {
    MavenRoboSettings.setMavenRepositoryId(originalMavenRepositoryId);
    MavenRoboSettings.setMavenRepositoryUrl(originalMavenRepositoryUrl);
    MavenRoboSettings.setMavenRepositoryUserName(originalMavenRepositoryUserName);
    MavenRoboSettings.setMavenRepositoryPassword(originalMavenRepositoryPassword);
  }

  @Test
  public void getMavenRepositoryId_defaultSonatype() {
    assertEquals("mavenCentral", MavenRoboSettings.getMavenRepositoryId());
  }

  @Test
  public void setMavenRepositoryId() {
    MavenRoboSettings.setMavenRepositoryId("testRepo");
    assertEquals("testRepo", MavenRoboSettings.getMavenRepositoryId());
  }

  @Test
  public void getMavenRepositoryUrl_defaultSonatype() {
    assertEquals("https://repo1.maven.org/maven2", MavenRoboSettings.getMavenRepositoryUrl());
  }

  @Test
  public void setMavenRepositoryUrl() {
    MavenRoboSettings.setMavenRepositoryUrl("http://local");
    assertEquals("http://local", MavenRoboSettings.getMavenRepositoryUrl());
  }

  @Test
  public void setMavenRepositoryUserName() {
    MavenRoboSettings.setMavenRepositoryUserName("username");
    assertEquals("username", MavenRoboSettings.getMavenRepositoryUserName());
  }

  @Test
  public void setMavenRepositoryPassword() {
    MavenRoboSettings.setMavenRepositoryPassword("password");
    assertEquals("password", MavenRoboSettings.getMavenRepositoryPassword());
  }
}
