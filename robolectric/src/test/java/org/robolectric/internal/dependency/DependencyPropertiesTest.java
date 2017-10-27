package org.robolectric.internal.dependency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DependencyPropertiesTest {

  @Test
  public void load() {
    DependencyProperties dependencyProperties = DependencyProperties.load();
    assertThat(dependencyProperties.getDependencyName(19)).isNotNull();
  }
}
