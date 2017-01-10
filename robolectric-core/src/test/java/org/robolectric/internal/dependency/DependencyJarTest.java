package org.robolectric.internal.dependency;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DependencyJarTest {
  @Test
  public void testGetShortName() throws Exception {
    assertThat(new DependencyJar("com.group", "artifact", "1.3", null).getShortName())
        .isEqualTo("com.group:artifact:1.3");
    assertThat(new DependencyJar("com.group", "artifact", "1.3", "dll").getShortName())
        .isEqualTo("com.group:artifact:1.3:dll");
  }
}