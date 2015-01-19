package org.robolectric.internal.bytecode;

import org.junit.Test;
import java.lang.annotation.Annotation;
import static org.assertj.core.api.Assertions.assertThat;

public class InstrumentingClassLoaderConfigTest {
  private final InstrumentingClassLoaderConfig config = new InstrumentingClassLoaderConfig();

  @Test
  public void shouldNotInstrumentAndroidAppClasses() throws Exception {
    assertThat(config.shouldInstrument(wrap("com.google.android.apps.Foo"))).isFalse();
  }

  @Test
  public void shouldInstrumentDalvikClasses() {
    assertThat(config.shouldInstrument(wrap("dalvik.system.DexFile"))).isTrue();
  }

  @Test
  public void shouldNotInstrumentCoreJdkClasses() throws Exception {
    assertThat(config.shouldInstrument(wrap("java.lang.Object"))).isFalse();
    assertThat(config.shouldInstrument(wrap("java.lang.String"))).isFalse();
  }

  @Test
  public void shouldInstrumentAndroidCoreClasses() throws Exception {
    assertThat(config.shouldInstrument(wrap("android.content.Intent"))).isTrue();
    assertThat(config.shouldInstrument(wrap("android.and.now.for.something.completely.different"))).isTrue();
  }

  @Test
  public void shouldNotAcquireRClasses() throws Exception {
    assertThat(config.shouldAcquire("com.whatever.Rfoo")).isTrue();
    assertThat(config.shouldAcquire("com.whatever.fooR")).isTrue();
    assertThat(config.shouldAcquire("com.whatever.R")).isFalse();
    assertThat(config.shouldAcquire("com.whatever.R$anything")).isFalse();
    assertThat(config.shouldAcquire("com.whatever.R$anything$else")).isTrue();
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
