package org.robolectric.internal.bytecode;

import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SetupTest {
  private final Setup setup = new Setup();

  @Test
  public void shouldNotInstrumentAndroidAppClasses() throws Exception {
    assertThat(setup.shouldInstrument(wrap("com.google.android.apps.Foo"))).isFalse();
  }

  @Test
  public void shouldInstrumentDalvikClasses() {
    assertThat(setup.shouldInstrument(wrap("dalvik.system.DexFile"))).isTrue();
  }

  @Test
  public void shouldNotInstrumentCoreJdkClasses() throws Exception {
    assertThat(setup.shouldInstrument(wrap("java.lang.Object"))).isFalse();
    assertThat(setup.shouldInstrument(wrap("java.lang.String"))).isFalse();
  }

  @Test
  public void shouldInstrumentAndroidCoreClasses() throws Exception {
    assertThat(setup.shouldInstrument(wrap("android.content.Intent"))).isTrue();
    assertThat(setup.shouldInstrument(wrap("android.and.now.for.something.completely.different"))).isTrue();
  }

  @Test
  public void shouldNotAcquireRClasses() throws Exception {
    assertThat(setup.shouldAcquire("com.whatever.Rfoo")).isTrue();
    assertThat(setup.shouldAcquire("com.whatever.fooR")).isTrue();
    assertThat(setup.shouldAcquire("com.whatever.R")).isFalse();
    assertThat(setup.shouldAcquire("com.whatever.R$anything")).isFalse();
    assertThat(setup.shouldAcquire("com.whatever.R$anything$else")).isTrue();
  }

  private ClassInfo wrap(final String className) {
    return new ClassInfo() {
      @Override public String getName() {
        return className;
      }

      @Override public boolean isInterface() {
        return false;
      }

      @Override public boolean isAnnotation() {
        return false;
      }

      @Override public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return false;
      }
    };
  }
}
