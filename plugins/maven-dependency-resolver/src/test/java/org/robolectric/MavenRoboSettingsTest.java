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
  private String originalMavenRepositoryProxyHost;
  private int originalMavenProxyPort;

  @Before
  public void setUp() {
    originalMavenRepositoryId = MavenRoboSettings.getMavenRepositoryId();
    originalMavenRepositoryUrl = MavenRoboSettings.getMavenRepositoryUrl();
    originalMavenRepositoryUserName = MavenRoboSettings.getMavenRepositoryUserName();
    originalMavenRepositoryPassword = MavenRoboSettings.getMavenRepositoryPassword();
    originalMavenRepositoryProxyHost = MavenRoboSettings.getMavenProxyHost();
    originalMavenProxyPort = MavenRoboSettings.getMavenProxyPort();
  }

  @After
  public void tearDown() {
    MavenRoboSettings.setMavenRepositoryId(originalMavenRepositoryId);
    MavenRoboSettings.setMavenRepositoryUrl(originalMavenRepositoryUrl);
    MavenRoboSettings.setMavenRepositoryUserName(originalMavenRepositoryUserName);
    MavenRoboSettings.setMavenRepositoryPassword(originalMavenRepositoryPassword);
    MavenRoboSettings.setMavenProxyHost(originalMavenRepositoryProxyHost);
    MavenRoboSettings.setMavenProxyPort(originalMavenProxyPort);
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

  @Test
  public void setMavenProxyHost() {
    MavenRoboSettings.setMavenProxyHost("123.4.5.678");
    assertEquals("123.4.5.678", MavenRoboSettings.getMavenProxyHost());
  }

  @Test
  public void setMavenProxyPort() {
    MavenRoboSettings.setMavenProxyPort(9000);
    assertEquals(9000, MavenRoboSettings.getMavenProxyPort());
  }
}
