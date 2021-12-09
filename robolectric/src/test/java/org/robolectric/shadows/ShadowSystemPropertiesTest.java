package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.SystemProperties;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowSystemPropertiesTest {

  @Test
  public void get() {
    assertThat(SystemProperties.get("ro.product.device")).isEqualTo("robolectric");
  }

  @Test
  public void getWithDefault() {
    assertThat(SystemProperties.get("foo", "bar")).isEqualTo("bar");
  }

  @Test
  public void getBoolean() {
    ShadowSystemProperties.override("false_1", "0");
    ShadowSystemProperties.override("false_2", "n");
    ShadowSystemProperties.override("false_3", "no");
    ShadowSystemProperties.override("false_4", "off");
    ShadowSystemProperties.override("false_5", "false");
    ShadowSystemProperties.override("true_1", "1");
    ShadowSystemProperties.override("true_2", "y");
    ShadowSystemProperties.override("true_3", "yes");
    ShadowSystemProperties.override("true_4", "on");
    ShadowSystemProperties.override("true_5", "true");
    ShadowSystemProperties.override("error_value", "error");

    assertThat(SystemProperties.getBoolean("false_1", true)).isFalse();
    assertThat(SystemProperties.getBoolean("false_2", true)).isFalse();
    assertThat(SystemProperties.getBoolean("false_3", true)).isFalse();
    assertThat(SystemProperties.getBoolean("false_4", true)).isFalse();
    assertThat(SystemProperties.getBoolean("false_5", true)).isFalse();
    assertThat(SystemProperties.getBoolean("true_1", false)).isTrue();
    assertThat(SystemProperties.getBoolean("true_2", false)).isTrue();
    assertThat(SystemProperties.getBoolean("true_3", false)).isTrue();
    assertThat(SystemProperties.getBoolean("true_4", false)).isTrue();
    assertThat(SystemProperties.getBoolean("true_5", false)).isTrue();
    assertThat(SystemProperties.getBoolean("error_value", false)).isFalse();
    assertThat(SystemProperties.getBoolean("error_value", true)).isTrue();
  }

  // The following readPropFromJarNotClassPathXX tests check build.prop is loaded from appropriate
  // android-all jar instead of loading build.prop from classpath aka LATEST_SDK.

  @Test
  @Config(sdk = 16)
  public void readPropFromJarNotClassPath16() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(16);
  }

  @Test
  @Config(sdk = 17)
  public void readPropFromJarNotClassPath17() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(17);
  }

  @Test
  @Config(sdk = 18)
  public void readPropFromJarNotClassPath18() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(18);
  }

  @Test
  @Config(sdk = 19)
  public void readPropFromJarNotClassPath19() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(19);
  }

  @Test
  @Config(sdk = 21)
  public void readPropFromJarNotClassPath21() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(21);
  }

  @Test
  @Config(sdk = 22)
  public void readPropFromJarNotClassPath22() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(22);
  }

  @Test
  @Config(sdk = 23)
  public void readPropFromJarNotClassPath23() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(23);
  }

  @Test
  @Config(sdk = 24)
  public void readPropFromJarNotClassPath24() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(24);
  }

  @Test
  @Config(sdk = 25)
  public void readPropFromJarNotClassPath25() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(25);
  }

  @Test
  @Config(sdk = 26)
  public void readPropFromJarNotClassPath26() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(26);
  }

  @Test
  @Config(sdk = 27)
  public void readPropFromJarNotClassPath27() {
    assertThat(SystemProperties.getInt("ro.build.version.sdk", 0)).isEqualTo(27);
  }

  @Test
  public void set() {
    assertThat(SystemProperties.get("newkey")).isEqualTo("");
    SystemProperties.set("newkey", "val");
    assertThat(SystemProperties.get("newkey")).isEqualTo("val");
    SystemProperties.set("newkey", null);
    assertThat(SystemProperties.get("newkey")).isEqualTo("");
  }

  @Test
  public void override() {
    assertThat(SystemProperties.get("newkey")).isEqualTo("");
    ShadowSystemProperties.override("newkey", "val");
    assertThat(SystemProperties.get("newkey")).isEqualTo("val");
    ShadowSystemProperties.override("newkey", null);
    assertThat(SystemProperties.get("newkey")).isEqualTo("");
  }
}
