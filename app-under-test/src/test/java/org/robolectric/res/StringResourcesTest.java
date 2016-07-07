package org.robolectric.res;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class StringResourcesTest {
  @Test
  public void escape_shouldEscapeStrings() {
    assertThat(StringResources.escape("\"This'll work\"")).isEqualTo("This'll work");
    assertThat(StringResources.escape("This\\'ll also work")).isEqualTo("This'll also work");
  }
}
