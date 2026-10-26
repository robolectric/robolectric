package org.robolectric.junit.platform.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;

class MirrorDescriptorTest {
  private static final UniqueId CLASS =
      UniqueId.forEngine("junit-jupiter").append("class", "com.example.MyTest");

  @Test
  void isDynamicContainer_forTemplatesAndFactories() {
    assertThat(MirrorDescriptor.isDynamicContainer(CLASS.append("test-template", "t(int)")))
        .isTrue();
    assertThat(MirrorDescriptor.isDynamicContainer(CLASS.append("test-factory", "f()"))).isTrue();
  }

  @Test
  void isDynamicContainer_falseForClassesAndMethods() {
    assertThat(MirrorDescriptor.isDynamicContainer(CLASS)).isFalse();
    assertThat(MirrorDescriptor.isDynamicContainer(CLASS.append("method", "test()"))).isFalse();
  }
}
