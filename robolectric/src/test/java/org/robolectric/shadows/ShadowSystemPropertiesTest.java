package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.SystemProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowSystemPropertiesTest {

  @Test
  public void get() {
    assertThat(SystemProperties.get("ro.build.fingerprint")).isEqualTo("robolectric");
  }

  @Test
  public void getWithDefault() {
    assertThat(SystemProperties.get("foo", "bar")).isEqualTo("bar");
  }

  @Test
  public void setProperty() {
    assertThat(SystemProperties.get("foo")).isEqualTo("");
    ShadowSystemProperties.setProperty("foo", "bar");
    assertThat(SystemProperties.get("foo")).isEqualTo("bar");
  }

  @Test
  public void setProperty_overrideExisting() {
    ShadowSystemProperties.setProperty("ro.product.device", "foo");
    assertThat(SystemProperties.get("ro.product.device")).isEqualTo("foo");
  }

  @Test
  public void removeProperty() {
    assertThat(SystemProperties.get("foo")).isEqualTo("");
    ShadowSystemProperties.setProperty("foo", "bar");
    assertThat(SystemProperties.get("foo")).isEqualTo("bar");
    ShadowSystemProperties.removeProperty("foo");
    assertThat(SystemProperties.get("foo")).isEqualTo("");
  }
}
