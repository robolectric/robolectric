package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

  @Test
  public void testGetPackageIdentifier() {
    assertThat(ResourceIds.getPackageIdentifier(0x01000000)).isEqualTo(0x01);
    assertThat(ResourceIds.getPackageIdentifier(0x7F000000)).isEqualTo(0x7F);
  }

  @Test
  public void testGetTypeIdentifier() {
    assertThat(ResourceIds.getTypeIdentifier(0x01019876)).isEqualTo(0x01);
    assertThat(ResourceIds.getTypeIdentifier(0x7F781234)).isEqualTo(0x78);
  }

  @Test
  public void testGetEntryIdentifier() {
    assertThat(ResourceIds.getEntryIdentifier(0x01019876)).isEqualTo(0x9876);
    assertThat(ResourceIds.getEntryIdentifier(0x7F781234)).isEqualTo(0x1234);
  }

  @Test
  public void testMakeIdentifier() {
    assertThat(ResourceIds.makeIdentifer(0x01, 0x01, 0x9876)).isEqualTo(0x01019876);
    assertThat(ResourceIds.makeIdentifer(0x7F, 0x78, 0x1234)).isEqualTo(0x7F781234);
  }
}
