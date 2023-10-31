package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.LocaleManager;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.TIRAMISU)
public final class ShadowLocaleManagerTest {
  private static final String DEFAULT_PACKAGE_NAME = "my.app";
  private static final LocaleList DEFAULT_LOCALES = LocaleList.forLanguageTags("en-XC,ar-XB");

  private Context context;
  private LocaleManager localeManager;
  private ShadowLocaleManager shadowLocaleManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    localeManager = context.getSystemService(LocaleManager.class);
    shadowLocaleManager = Shadow.extract(localeManager);
  }

  @Test
  public void setApplicationLocales_updatesMap() {
    // empty map before set is called.
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(LocaleList.getEmptyLocaleList());

    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);

    shadowLocaleManager.enforceInstallerCheck(false);
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void setApplicationLocales_defaultPackage_updatesMap() {
    // empty map before set is called.
    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(LocaleList.getEmptyLocaleList());

    localeManager.setApplicationLocales(DEFAULT_LOCALES);

    shadowLocaleManager.enforceInstallerCheck(false);
    assertThat(localeManager.getApplicationLocales()).isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void getApplicationLocales_fetchAsInstaller_returnsLocales() {
    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);
    shadowLocaleManager.setCallerAsInstallerForPackage(DEFAULT_PACKAGE_NAME);
    shadowLocaleManager.enforceInstallerCheck(true);

    assertThat(localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME))
        .isEqualTo(DEFAULT_LOCALES);
  }

  @Test
  public void getApplicationLocales_fetchAsInstaller_throwsSecurityExceptionIfIncorrectInstaller() {
    localeManager.setApplicationLocales(DEFAULT_PACKAGE_NAME, DEFAULT_LOCALES);
    shadowLocaleManager.enforceInstallerCheck(true);

    assertThrows(
        SecurityException.class, () -> localeManager.getApplicationLocales(DEFAULT_PACKAGE_NAME));
  }

  @Test
  @Config(qualifiers = "en")
  public void getSystemLocales_en() {
    LocaleList localeList = localeManager.getSystemLocales();
    assertThat(localeList.size()).isEqualTo(1);
    assertThat(localeList.get(0).getLanguage()).isEqualTo("en");
  }

  @Test
  @Config(qualifiers = "zh")
  public void getSystemLocales_zh() {
    LocaleList localeList = localeManager.getSystemLocales();
    assertThat(localeList.size()).isEqualTo(1);
    assertThat(localeList.get(0).getLanguage()).isEqualTo("zh");
  }
}
