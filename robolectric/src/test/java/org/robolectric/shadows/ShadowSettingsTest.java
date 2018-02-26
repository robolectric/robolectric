package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.assertj.core.api.Assertions.assertThat;

import android.content.ContentResolver;
import android.provider.Settings;
import android.text.format.DateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowSettingsTest {
  private ContentResolver contentResolver;

  @Before
  public void setUp() {
    contentResolver = RuntimeEnvironment.application.getContentResolver();
  }

  @Test
  public void testSystemGetInt() {
    assertThat(Settings.System.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.System.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.System.putInt(contentResolver, "property", 1);
    assertThat(Settings.System.getInt(contentResolver, "property", 0)).isEqualTo(1);

    Settings.System.putString(contentResolver, "property", "11");
    assertThat(Settings.System.getInt(contentResolver, "property", 0)).isEqualTo(11);
  }

  @Test
  public void testSecureGetInt() {
    assertThat(Settings.Secure.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.Secure.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.Secure.putInt(contentResolver, "property", 1);
    assertThat(Settings.Secure.getInt(contentResolver, "property", 0)).isEqualTo(1);

    Settings.Secure.putString(contentResolver, "property", "11");
    assertThat(Settings.Secure.getInt(contentResolver, "property", 0)).isEqualTo(11);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testGlobalGetInt() {
    assertThat(Settings.Global.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.Global.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.Global.putInt(contentResolver, "property", 1);
    assertThat(Settings.Global.getInt(contentResolver, "property", 0)).isEqualTo(1);

    Settings.Global.putString(contentResolver, "property", "11");
    assertThat(Settings.Global.getInt(contentResolver, "property", 0)).isEqualTo(11);
  }

  @Test
  public void testSystemGetString() {
    assertThat(Settings.System.getString(contentResolver, "property")).isNull();

    Settings.System.putString(contentResolver, "property", "value");
    assertThat(Settings.System.getString(contentResolver, "property")).isEqualTo("value");

    Settings.System.putInt(contentResolver, "property", 123);
    assertThat(Settings.System.getString(contentResolver, "property")).isEqualTo("123");

    Settings.System.putLong(contentResolver, "property", 456L);
    assertThat(Settings.System.getString(contentResolver, "property")).isEqualTo("456");

    Settings.System.putFloat(contentResolver, "property", 7.89f);
    assertThat(Settings.System.getString(contentResolver, "property")).isEqualTo("7.89");
  }

  @Test
  public void testSystemGetLong() throws Settings.SettingNotFoundException {
    assertThat(Settings.System.getLong(contentResolver, "property", 10L)).isEqualTo(10L);
    Settings.System.putLong(contentResolver, "property", 42L);
    assertThat(Settings.System.getLong(contentResolver, "property")).isEqualTo(42L);
    assertThat(Settings.System.getLong(contentResolver, "property", 10L)).isEqualTo(42L);

    Settings.System.putString(contentResolver, "property", "11");
    assertThat(Settings.System.getLong(contentResolver, "property", 0)).isEqualTo(11L);
  }

  @Test
  public void testSystemGetFloat() {
    assertThat(Settings.System.getFloat(contentResolver, "property", 23.23f)).isEqualTo(23.23f);
    Settings.System.putFloat(contentResolver, "property", 42.42f);
    assertThat(Settings.System.getFloat(contentResolver, "property", 10L)).isEqualTo(42.42f);

    Settings.System.putString(contentResolver, "property", "11.2");
    assertThat(Settings.System.getFloat(contentResolver, "property", 0)).isEqualTo(11.2f);
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetLong_exception() throws Settings.SettingNotFoundException {
    Settings.System.getLong(contentResolver, "property");
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetInt_exception() throws Settings.SettingNotFoundException {
    Settings.System.getInt(contentResolver, "property");
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetFloat_exception() throws Settings.SettingNotFoundException {
    Settings.System.getFloat(contentResolver, "property");
  }

  @Test
  public void testSet24HourMode_24() {
    ShadowSettings.set24HourTimeFormat(true);
    assertThat(DateFormat.is24HourFormat(RuntimeEnvironment.application)).isTrue();
  }

  @Test
  public void testSet24HourMode_12() {
    ShadowSettings.set24HourTimeFormat(false);
    assertThat(DateFormat.is24HourFormat(RuntimeEnvironment.application)).isFalse();
  }
}
