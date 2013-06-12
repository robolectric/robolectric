package org.robolectric.shadows;


import android.content.res.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ConfigurationTest {

  private Configuration configuration;
  private ShadowConfiguration shConfiguration;

  @Before
  public void setUp() throws Exception {
    configuration = new Configuration();
    shConfiguration = Robolectric.shadowOf( configuration );
  }

  @Test
  public void testSetToDefaults() throws Exception {
    configuration.setToDefaults();
    assertThat(configuration.screenLayout).isEqualTo(Configuration.SCREENLAYOUT_LONG_NO | Configuration.SCREENLAYOUT_SIZE_NORMAL);
  }

  @Test
  public void testSetLocale() {
    shConfiguration.setLocale( Locale.US );
    assertThat(configuration.locale).isEqualTo(Locale.US);

    shConfiguration.setLocale( Locale.FRANCE);
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
