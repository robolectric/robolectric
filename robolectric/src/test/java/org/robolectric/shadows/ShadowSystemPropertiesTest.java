package org.robolectric.shadows;

import android.os.SystemProperties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.internal.SdkConfig;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowSystemPropertiesTest {
  @Test
  public void buildVersionSdk() throws Exception {
    assertThat(SystemProperties.get("ro.build.version.sdk")).isEqualTo("" + RuntimeEnvironment.getApiLevel());
  }

  @Test @Ignore
  public void buildVersionRelease() throws Exception {
    SdkConfig expectedSdkConfig = new SdkConfig(RuntimeEnvironment.getApiLevel());
    assertThat(SystemProperties.get("ro.build.version.release")).isEqualTo(expectedSdkConfig.getAndroidVersion());
  }

  @Test
  public void getString() throws Exception {
    assertThat(SystemProperties.get("ro.secure")).isEqualTo("1");
    assertThat(SystemProperties.get("ro.secure", "def")).isEqualTo("1");
    assertThat(SystemProperties.get("nonexistant", "def")).isEqualTo("def");
    assertThat(SystemProperties.get("nonexistant")).isEqualTo("");
  }

  @Test
  public void getInt() throws Exception {
    assertThat(SystemProperties.getInt("ro.secure", -1)).isEqualTo(1);
    assertThat(SystemProperties.getInt("nonexistant", -1)).isEqualTo(-1);
    assertThat(SystemProperties.getInt("ro.build.version.all_codenames", -1234)).isEqualTo(-1234);
  }

  @Test
  public void getLong() throws Exception {
    assertThat(SystemProperties.getLong("ro.secure", -1)).isEqualTo(1);
    assertThat(SystemProperties.getLong("ro.build.date.utc", -1)).isEqualTo(1277708400000L);
    assertThat(SystemProperties.getLong("nonexistant", -1234)).isEqualTo(-1234);
    assertThat(SystemProperties.getLong("ro.build.version.all_codenames", -1234)).isEqualTo(-1234);
  }

  @Test
  public void getBoolean() throws Exception {
    assertThat(SystemProperties.getBoolean("ro.secure", false)).isEqualTo(true);
    assertThat(SystemProperties.getBoolean("debug.choreographer.vsync", false)).isEqualTo(false);
    assertThat(SystemProperties.getBoolean("ro.build.version.all_codenames", true)).isEqualTo(true);
  }


  @Test
  public void set() throws Exception {
    SystemProperties.set("some-value", "abc");
    assertThat(SystemProperties.get("some-value")).isEqualTo("abc");
  }

  @Test
  public void shouldResetChangeListeners() throws Exception {
    final List<String> log = new ArrayList<>();
    SystemProperties.addChangeCallback(new Runnable() {
      @Override
      public void run() {
        log.add("changed");
      }
    });

    SystemProperties.set("some-value", "123");
    assertThat(log).containsExactly("changed");

    SystemProperties.set("some-value", "123");
    assertThat(log).containsExactly("changed", "changed");

    ShadowSystemProperties.reset();

    SystemProperties.set("some-value", "123");
    assertThat(log).containsExactly("changed", "changed");
  }
}
