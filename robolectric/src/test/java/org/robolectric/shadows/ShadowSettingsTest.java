package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.text.format.DateFormat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowSettingsTest {

  private ContentResolver contentResolver;
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    contentResolver = context.getContentResolver();
  }

  @Test
  public void testSystemGetInt() throws Exception {
    assertThat(Settings.System.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.System.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.System.putInt(contentResolver, "property", 1);
    assertThat(Settings.System.getInt(contentResolver, "property", 0)).isEqualTo(1);
  }

  @Test
  public void testSecureGetInt() throws Exception {
    assertThat(Settings.Secure.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.Secure.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.Secure.putInt(contentResolver, "property", 1);
    assertThat(Settings.Secure.getInt(contentResolver, "property", 0)).isEqualTo(1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testGlobalGetInt() throws Exception {
    assertThat(Settings.Global.getInt(contentResolver, "property", 0)).isEqualTo(0);
    assertThat(Settings.Global.getInt(contentResolver, "property", 2)).isEqualTo(2);

    Settings.Global.putInt(contentResolver, "property", 1);
    assertThat(Settings.Global.getInt(contentResolver, "property", 0)).isEqualTo(1);
  }

  @Test
  public void testSystemGetString() throws Exception {
    assertThat(Settings.System.getString(contentResolver, "property")).isNull();

    Settings.System.putString(contentResolver, "property", "value");
    assertThat(Settings.System.getString(contentResolver, "property")).isEqualTo("value");
  }

  @Test
  public void testSystemGetLong() throws Exception {
    assertThat(Settings.System.getLong(contentResolver, "property", 10L)).isEqualTo(10L);
    Settings.System.putLong(contentResolver, "property", 42L);
    assertThat(Settings.System.getLong(contentResolver, "property")).isEqualTo(42L);
    assertThat(Settings.System.getLong(contentResolver, "property", 10L)).isEqualTo(42L);
  }

  @Test
  public void testSystemGetFloat() throws Exception {
    assertThat(Settings.System.getFloat(contentResolver, "property", 23.23f)).isEqualTo(23.23f);
    Settings.System.putFloat(contentResolver, "property", 42.42f);
    assertThat(Settings.System.getFloat(contentResolver, "property", 10L)).isEqualTo(42.42f);
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetLong_exception() throws Exception {
    Settings.System.getLong(contentResolver, "property");
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetInt_exception() throws Exception {
    Settings.System.getInt(contentResolver, "property");
  }

  @Test(expected = Settings.SettingNotFoundException.class)
  public void testSystemGetFloat_exception() throws Exception {
    Settings.System.getFloat(contentResolver, "property");
  }

  @Test
  public void testSet24HourMode_24() {
    ShadowSettings.set24HourTimeFormat(true);
    assertThat(DateFormat.is24HourFormat(context.getBaseContext())).isTrue();
  }

  @Test
  public void testSet24HourMode_12() {
    ShadowSettings.set24HourTimeFormat(false);
    assertThat(DateFormat.is24HourFormat(context.getBaseContext())).isFalse();
  }

  @Test
  public void testSetAdbEnabled_settingsSecure_true() {
    ShadowSettings.setAdbEnabled(true);
    assertThat(Secure.getInt(context.getContentResolver(), Secure.ADB_ENABLED, 0)).isEqualTo(1);
  }

  @Test
  public void testSetAdbEnabled_settingsSecure_false() {
    ShadowSettings.setAdbEnabled(false);
    assertThat(Secure.getInt(context.getContentResolver(), Secure.ADB_ENABLED, 1)).isEqualTo(0);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testSetAdbEnabled_sinceJBMR1_settingsGlobal_true() {
    ShadowSettings.setAdbEnabled(true);
    assertThat(Global.getInt(context.getContentResolver(), Global.ADB_ENABLED, 0)).isEqualTo(1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testSetAdbEnabled_sinceJBMR1_settingsGlobal_false() {
    ShadowSettings.setAdbEnabled(false);
    assertThat(Global.getInt(context.getContentResolver(), Global.ADB_ENABLED, 1)).isEqualTo(0);
  }

  @Test
  public void testSetInstallNonMarketApps_settingsSecure_true() {
    ShadowSettings.setInstallNonMarketApps(true);
    assertThat(Secure.getInt(context.getContentResolver(), Secure.INSTALL_NON_MARKET_APPS, 0))
        .isEqualTo(1);
  }

  @Test
  public void testSetInstallNonMarketApps_settingsSecure_false() {
    ShadowSettings.setInstallNonMarketApps(false);
    assertThat(Secure.getInt(context.getContentResolver(), Secure.INSTALL_NON_MARKET_APPS, 1))
        .isEqualTo(0);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testSetInstallNonMarketApps_sinceJBMR1_settingsGlobal_true() {
    ShadowSettings.setInstallNonMarketApps(true);
    assertThat(Global.getInt(context.getContentResolver(), Global.INSTALL_NON_MARKET_APPS, 0))
        .isEqualTo(1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testSetInstallNonMarketApps_sinceJBMR1_settingsGlobal_false() {
    ShadowSettings.setInstallNonMarketApps(false);
    assertThat(Global.getInt(context.getContentResolver(), Global.INSTALL_NON_MARKET_APPS, 1))
        .isEqualTo(0);
  }
}
