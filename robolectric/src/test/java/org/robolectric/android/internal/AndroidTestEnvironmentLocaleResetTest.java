package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.LocaleList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Tests for {@link org.robolectric.android.internal.AndroidTestEnvironment} that verifies the
 * Locale is reset after the test suite has completed.
 */
@RunWith(AndroidJUnit4.class)
public class AndroidTestEnvironmentLocaleResetTest {

  private static Locale initialLocale;

  @BeforeClass
  public static void beforeClass() {
    initialLocale = Locale.getDefault();
  }

  @AfterClass
  public static void afterClass() {
    assertThat(Locale.getDefault()).isEqualTo(initialLocale);
  }

  @Config(qualifiers = "ar-rEG")
  @Test
  public void locale_changed() {
    assertThat(Locale.getDefault()).isEqualTo(Locale.forLanguageTag("ar-EG"));
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      assertThat(LocaleList.getDefault()).isEqualTo(LocaleList.forLanguageTags("ar-EG"));
    }
  }

  @Config(qualifiers = "ja-rJP")
  @Test
  public void locale_changed_differentConfig() {
    assertThat(Locale.getDefault()).isEqualTo(Locale.forLanguageTag("ja-JP"));
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.N) {
      assertThat(LocaleList.getDefault()).isEqualTo(LocaleList.forLanguageTags("ja-JP"));
    }
  }

  @Test
  public void locale_changed_byTestCode() {
    Locale.setDefault(Locale.forLanguageTag("ar-EG"));
  }
}
