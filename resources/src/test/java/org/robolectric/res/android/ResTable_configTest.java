package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResTable_configTest {

  @Test
  public void testLocale() throws Exception {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 'e';
    resTable_config.language[1] = 'n';
    resTable_config.country[0] = 'u';
    resTable_config.country[1] = 'k';

    assertThat(resTable_config.locale())
        .isEqualTo(('e' << 24) | ('n' << 16) | ('u' << 8) | 'k');
  }

  @Test
  public void getBcp47Locale_shouldReturnCanonicalizedTag() {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 'j';
    resTable_config.language[1] = 'a';
    resTable_config.country[0] = 'j';
    resTable_config.country[1] = 'p';

    assertThat(resTable_config.getBcp47Locale(/* canonicalize= */ true)).isEqualTo("ja-jp");
  }

  @Test
  public void getBcp47Locale_philippines_shouldReturnFil() {
    ResTable_config resTable_config = new ResTable_config();
    resTable_config.language[0] = 't';
    resTable_config.language[1] = 'l';
    resTable_config.country[0] = 'p';
    resTable_config.country[1] = 'h';

    assertThat(resTable_config.getBcp47Locale(/* canonicalize= */ true)).isEqualTo("fil-ph");
  }
}
