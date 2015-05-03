package org.robolectric.internal.bytecode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentationConfigurationTest {
  private final InstrumentationConfiguration config = InstrumentationConfiguration.newBuilder().build();

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
  public void shouldInstrumentOrgApacheHttpClasses() {
    assertThat(config.shouldInstrument(wrap("org.apache.http.util.CharArrayBuffer"))).isTrue();
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
    ClassInfo info = mock(ClassInfo.class);
    when(info.getName()).thenReturn(className);
    return info;
  }
}
