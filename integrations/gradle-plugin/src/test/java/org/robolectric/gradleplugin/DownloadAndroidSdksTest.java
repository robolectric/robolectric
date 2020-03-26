package org.robolectric.gradleplugin;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.gradleplugin.GradlePluginTest.mapOf;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

public class DownloadAndroidSdksTest {

  private Properties defaultSdks;

  @Before
  public void setUp() throws Exception {
    defaultSdks = new Properties();
    defaultSdks.setProperty("17", "sdk17.jar");
    defaultSdks.setProperty("18", "sdk18.jar");
    defaultSdks.setProperty("19", "sdk19.jar");
  }

  @Test
  public void figureSdksAcceptsStrings() throws Exception {
    assertThat(DownloadAndroidSdks.figureSdks("17", defaultSdks))
        .isEqualTo(mapOf(17, "sdk17.jar"));
  }

  @Test
  public void figureSdksAcceptsInts() throws Exception {
    assertThat(DownloadAndroidSdks.figureSdks(18, defaultSdks))
        .isEqualTo(mapOf(18, "sdk18.jar"));
  }

  @Test
  public void figureSdksAcceptsIntArrays() throws Exception {
    assertThat(DownloadAndroidSdks.figureSdks(new int[]{17, 19}, defaultSdks))
        .isEqualTo(mapOf(17, "sdk17.jar", 19, "sdk19.jar"));
  }

  @Test
  public void figureSdksAcceptsIntLists() throws Exception {
    assertThat(DownloadAndroidSdks.figureSdks(Arrays.asList(17, 19), defaultSdks))
        .isEqualTo(mapOf(17, "sdk17.jar", 19, "sdk19.jar"));
  }

  @Test
  public void figureSdksAcceptsMapWithCoordinates() throws Exception {
    Map<Integer, Object> config = mapOf(
        17, "org.xxx:mysdk:1.2.3",
        19, "/path/to/another19.jar",
        20, new File("/path/somewhere.jar")
    );

    assertThat(DownloadAndroidSdks.figureSdks(config, defaultSdks))
        .isEqualTo(mapOf(
            17, "org.xxx:mysdk:1.2.3",
            19, new File("/path/to/another19.jar"),
            20, new File("/path/somewhere.jar")
        ));
  }
}
