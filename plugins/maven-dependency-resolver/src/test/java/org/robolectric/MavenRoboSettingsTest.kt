package org.robolectric

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MavenRoboSettingsTest {

  private var originalMavenRepositoryId: String? = null
  private var originalMavenRepositoryUrl: String? = null
  private var originalMavenRepositoryUserName: String? = null
  private var originalMavenRepositoryPassword: String? = null

  @Before
  fun setUp() {
    originalMavenRepositoryId = MavenRoboSettings.getMavenRepositoryId()
    originalMavenRepositoryUrl = MavenRoboSettings.getMavenRepositoryUrl()
    originalMavenRepositoryUserName = MavenRoboSettings.getMavenRepositoryUserName()
    originalMavenRepositoryPassword = MavenRoboSettings.getMavenRepositoryPassword()
  }

  @After
  fun tearDown() {
    MavenRoboSettings.setMavenRepositoryId(originalMavenRepositoryId)
    MavenRoboSettings.setMavenRepositoryUrl(originalMavenRepositoryUrl)
    MavenRoboSettings.setMavenRepositoryUserName(originalMavenRepositoryUserName)
    MavenRoboSettings.setMavenRepositoryPassword(originalMavenRepositoryPassword)
  }

  @Test
  fun getMavenRepositoryId_defaultSonatype() {
    assertEquals("mavenCentral", MavenRoboSettings.getMavenRepositoryId())
  }

  @Test
  fun setMavenRepositoryId() {
    MavenRoboSettings.setMavenRepositoryId("testRepo")
    assertEquals("testRepo", MavenRoboSettings.getMavenRepositoryId())
  }

  @Test
  fun getMavenRepositoryUrl_defaultSonatype() {
    assertEquals("https://repo1.maven.org/maven2", MavenRoboSettings.getMavenRepositoryUrl())
  }

  @Test
  fun setMavenRepositoryUrl() {
    MavenRoboSettings.setMavenRepositoryUrl("http://local")
    assertEquals("http://local", MavenRoboSettings.getMavenRepositoryUrl())
  }

  @Test
  fun setMavenRepositoryUserName() {
    MavenRoboSettings.setMavenRepositoryUserName("username")
    assertEquals("username", MavenRoboSettings.getMavenRepositoryUserName())
  }

  @Test
  fun setMavenRepositoryPassword() {
    MavenRoboSettings.setMavenRepositoryPassword("password")
    assertEquals("password", MavenRoboSettings.getMavenRepositoryPassword())
  }
}
