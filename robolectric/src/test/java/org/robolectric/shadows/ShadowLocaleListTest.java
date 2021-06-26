package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.os.LocaleList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link org.robolectric.shadows.ShadowLocaleList} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.N)
public class ShadowLocaleListTest {

  @Before
  public void setUp() {
    assertThat(LocaleList.getDefault().size()).isEqualTo(1);
  }

  @Test
  public void testResetter() {
    assertThat(LocaleList.getDefault().toLanguageTags()).doesNotContain("IN");

    Locale locale = new Locale("en", "IN");
    LocaleList.setDefault(new LocaleList(locale));
    assertThat(LocaleList.getDefault().toLanguageTags()).contains("IN");

    ShadowLocaleList.reset();

    Locale.setDefault(Locale.US);
    assertThat(LocaleList.getDefault().toLanguageTags()).doesNotContain("IN");
  }
}
