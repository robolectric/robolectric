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
    assertThat(shadowOf(application.getAssets()).getQualifiers()).isEqualTo("en");
  }

  @Config(qualifiers = "fr")
  @Test public void shouldGetFromMethod() throws Exception {
    assertThat(shadowOf(application.getAssets()).getQualifiers()).isEqualTo("fr");
  }
}
