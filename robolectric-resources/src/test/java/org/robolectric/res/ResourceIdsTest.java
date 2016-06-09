package org.robolectric.res;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResourceIds}
 */
public class ResourceIdsTest {

  @Test
  public void testIsFrameworkResource() {
    assertThat(ResourceIds.isFrameworkResource(0x01000000)).isTrue();
    assertThat(ResourceIds.isFrameworkResource(0x01223333)).isTrue();
    assertThat(ResourceIds.isFrameworkResource(0x01FFFFFF)).isTrue();

    assertThat(ResourceIds.isFrameworkResource(0x7F000000)).isFalse();
    assertThat(ResourceIds.isFrameworkResource(0x7F223333)).isFalse();
    assertThat(ResourceIds.isFrameworkResource(0x7FFFFFFF)).isFalse();
  }
}
