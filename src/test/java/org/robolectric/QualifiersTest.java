package org.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
@Config(qualifiers = "en")
public class QualifiersTest {
  @Test public void shouldGetFromClass() throws Exception {
    String expectedQualifiers = "en" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertThat(shadowOf(application.getAssets()).getQualifiers()).isEqualTo(expectedQualifiers);
  }

  @Config(qualifiers = "fr")
  @Test public void shouldGetFromMethod() throws Exception {
    String expectedQualifiers = "fr" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertThat(shadowOf(application.getAssets()).getQualifiers()).isEqualTo(expectedQualifiers);
  }

  @Config(qualifiers = "de")
  @Test public void getQuantityString() throws Exception {
    assertThat(application.getResources().getQuantityString(R.plurals.minute, 2)).isEqualTo(application.getResources().getString(R.string.minute_plural));
  }
}
