package org.robolectric.res;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ResNameTest {
  @Test public void shouldQualify() throws Exception {
    assertThat(ResName.qualifyResourceName("some.package:type/name", null, null)).isEqualTo("some.package:type/name");
    assertThat(ResName.qualifyResourceName("some.package:type/name", "default.package", "deftype")).isEqualTo("some.package:type/name");
    assertThat(ResName.qualifyResourceName("some.package:name", "default.package", "deftype")).isEqualTo("some.package:deftype/name");
    assertThat(ResName.qualifyResourceName("type/name", "default.package", "deftype")).isEqualTo("default.package:type/name");
    assertThat(ResName.qualifyResourceName("name", "default.package", "deftype")).isEqualTo("default.package:deftype/name");
  }

  @Test
  public void qualifyFromFilePathShouldExtractResourceTypeAndNameFromUnqualifiedPath() {
    final ResName actual = ResName.qualifyFromFilePath("some.package", "./res/drawable/icon.png");
    assertThat(actual.getFullyQualifiedName()).isEqualTo("some.package:drawable/icon");
  }

  @Test
  public void qualifyFromFilePathShouldExtractResourceTypeAndNameFromQualifiedPath() {
    final ResName actual = ResName.qualifyFromFilePath("some.package", "./res/drawable-hdpi/icon.png");
    assertThat(actual.getFullyQualifiedName()).isEqualTo("some.package:drawable/icon");
  }
}
