package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class EmptyResourceLoaderTest {
  @Test
  public void shouldProvideForNameSpace() throws Exception {

    EmptyResourceLoader resourceLoader = new EmptyResourceLoader("android", null);
    assertThat(resourceLoader.providesFor("android")).isTrue();
    assertThat(resourceLoader.providesFor("org.robolectric")).isFalse();
  }
}
