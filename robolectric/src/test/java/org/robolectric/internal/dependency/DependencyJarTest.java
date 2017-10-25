package org.robolectric.internal.dependency;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Test
  public void fromShortName() {
    DependencyJar dependencyJar = DependencyJar.fromShortName("com.group:artifact:1.3");
    assertThat(dependencyJar.getGroupId()).isEqualTo("com.group");
    assertThat(dependencyJar.getArtifactId()).isEqualTo("artifact");
    assertThat(dependencyJar.getVersion()).isEqualTo("1.3");
    assertThat(dependencyJar.getClassifier()).isNull();
  }

  @Test
  public void fromShortName_classifier() {
    DependencyJar dependencyJar = DependencyJar.fromShortName("com.group:artifact:1.3:dll");
    assertThat(dependencyJar.getGroupId()).isEqualTo("com.group");
    assertThat(dependencyJar.getArtifactId()).isEqualTo("artifact");
    assertThat(dependencyJar.getVersion()).isEqualTo("1.3");
    assertThat(dependencyJar.getClassifier()).isEqualTo("dll");
  }
}