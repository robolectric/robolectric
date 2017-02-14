package org.robolectric.shadows;


import android.content.res.Configuration;
import android.os.Build;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static android.content.res.Configuration.SCREENLAYOUT_UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowConfigurationTest {

  private Configuration configuration;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
  }

  @Test
  public void setToDefaultsShouldSetRealDefaults() {
    configuration.setToDefaults();
    assertThat(configuration.fontScale).isEqualTo(1);
    assertThat(configuration.screenLayout).isEqualTo(SCREENLAYOUT_UNDEFINED);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void testSetLocale() {
    configuration.setLocale( Locale.US );
    assertThat(configuration.locale).isEqualTo(Locale.US);

    configuration.setLocale( Locale.FRANCE);
    assertThat(configuration.locale).isEqualTo(Locale.FRANCE);
  }

  @Test
  public void testConstructCopy() {
    configuration.setToDefaults();
    Configuration clone = new Configuration(configuration);
    assertThat(configuration).isEqualTo(clone);
  }

  @Test public void testToString_shouldntExplode() throws Exception {
    assertThat(new Configuration().toString()).contains("mcc");
  }
}
