package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResourceIds}
 */
@RunWith(JUnit4.class)
public class ResourceIdsTest {
  @Test
  public void testIsFrameworkResource() {
    assertThat(ResourceIds.isFrameworkResource(0x01000000)).isTrue();
    assertThat(ResourceIds.isFrameworkResource(0x7F000000)).isFalse();
  }
}
