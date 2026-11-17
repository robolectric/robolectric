package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.LocaleList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link org.robolectric.shadows.ShadowLocaleList} */
@RunWith(AndroidJUnit4.class)
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
