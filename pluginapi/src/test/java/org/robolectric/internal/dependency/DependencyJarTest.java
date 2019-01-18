package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DependencyJarTest {
  @Test
  public void testGetShortName() throws Exception {
    assertThat(new DependencyJar("com.group", "artifact", "1.3", null).getShortName())
        .isEqualTo("com.group:artifact:1.3");
    assertThat(new DependencyJar("com.group", "artifact", "1.3", "dll").getShortName())
        .isEqualTo("com.group:artifact:1.3:dll");
  }
}