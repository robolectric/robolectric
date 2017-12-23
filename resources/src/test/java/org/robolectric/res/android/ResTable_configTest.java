package org.robolectric.res.android;

import static org.assertj.core.api.Assertions.assertThat;

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
}