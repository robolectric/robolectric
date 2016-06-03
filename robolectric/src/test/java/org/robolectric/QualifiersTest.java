package org.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@Config(qualifiers = "en")
@RunWith(TestRunners.WithDefaults.class)
public class QualifiersTest {

  @Test
  public void shouldGetFromClass() throws Exception {
    String expectedQualifiers = "en" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertThat(RuntimeEnvironment.getQualifiers()).isEqualTo(expectedQualifiers);
  }

  @Test @Config(qualifiers = "fr")
  public void shouldGetFromMethod() throws Exception {
    String expectedQualifiers = "fr" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertThat(RuntimeEnvironment.getQualifiers()).isEqualTo(expectedQualifiers);
  }

  @Test @Config(qualifiers = "de")
  public void getQuantityString() throws Exception {
    assertThat(RuntimeEnvironment.application.getResources().getQuantityString(R.plurals.minute, 2)).isEqualTo(RuntimeEnvironment.application.getResources().getString(R.string.minute_plural));
  }
}
