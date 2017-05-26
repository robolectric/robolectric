package org.robolectric.shadows;

import android.os.SystemProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowSystemPropertiesTest {
  @Test
  public void getString() throws Exception {
    assertThat(SystemProperties.get("ro.build.version.sdk")).isEqualTo("8");
    assertThat(SystemProperties.get("ro.build.version.sdk", "def")).isEqualTo("8");
    assertThat(SystemProperties.get("ro.build.version.sdkzzz", "def")).isEqualTo("def");
    assertThat(SystemProperties.get("nonexistant")).isEqualTo("");
  }

  @Test
  public void getInt() throws Exception {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", -1)).isEqualTo(8);
    assertThat(SystemProperties.getInt("ro.build.version.sdkzzz", -1)).isEqualTo(-1);
  }

  @Test
  public void getLong() throws Exception {
    assertThat(SystemProperties.getLong("ro.build.version.sdk", -1)).isEqualTo(8);
    assertThat(SystemProperties.getLong("ro.build.date.utc", -1)).isEqualTo(1277708400000L);
    assertThat(SystemProperties.getLong("ro.build.date.utczzz", -1234)).isEqualTo(-1234);
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
