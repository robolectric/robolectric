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
}
