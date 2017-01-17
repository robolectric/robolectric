package org.robolectric.internal.bytecode;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstrumentationConfigurationTest {
  private InstrumentationConfiguration config;

  @Before
  public void setUp() throws Exception {
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
    RobolectricTestRunner.configure(builder);
    config = builder.build();
  }

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
  public void shouldInstrumentOrgKxmlClasses() {
    assertThat(config.shouldInstrument(wrap("org.kxml2.io.KXmlParser"))).isTrue();
  }

  @Test
  public void shouldNotAcquireRClasses() throws Exception {
    assertThat(config.shouldAcquire("com.whatever.Rfoo")).isTrue();
    assertThat(config.shouldAcquire("com.whatever.fooR")).isTrue();
    assertThat(config.shouldAcquire("com.whatever.R")).isFalse();
    assertThat(config.shouldAcquire("com.whatever.R$anything")).isFalse();
    assertThat(config.shouldAcquire("com.whatever.R$anything$else")).isTrue();
  }

  @Test
  public void shouldNotAcquireExcludedPackages() throws Exception {
    assertThat(config.shouldAcquire("scala.Test")).isFalse();
    assertThat(config.shouldAcquire("scala.util.Test")).isFalse();
    assertThat(config.shouldAcquire("org.specs2.whatever.foo")).isFalse();
    assertThat(config.shouldAcquire("com.almworks.sqlite4java.whatever.Cls$anything$else")).isFalse();
  }

  @Test
  public void shouldInstrumentCustomClasses() throws Exception {
    String instrumentName = "com.whatever.SomeClassNameToInstrument";
    String notInstrumentName = "com.whatever.DoNotInstruementMe";
    InstrumentationConfiguration customConfig = InstrumentationConfiguration.newBuilder().addInstrumentedClass(instrumentName).build();
    assertThat(customConfig.shouldInstrument(wrap(instrumentName))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap(notInstrumentName))).isFalse();
  }

  @Test
  public void equals_ShouldCheckClassNames() throws Exception {
    String instrumentName = "com.whatever.SomeClassNameToInstrument";
    InstrumentationConfiguration baseConfig = InstrumentationConfiguration.newBuilder().build();
    InstrumentationConfiguration customConfig = InstrumentationConfiguration.newBuilder().addInstrumentedClass(instrumentName).build();

    assertThat(baseConfig).isNotEqualTo(customConfig);
  }

  public void shouldNotInstrumentListedClasses() throws Exception {
    String instrumentName = "android.foo.bar";
    InstrumentationConfiguration customConfig = InstrumentationConfiguration.newBuilder().doNotInstrumentClass(instrumentName).build();

    assertThat(customConfig.shouldInstrument(wrap(instrumentName))).isFalse();
  }

  private ClassInfo wrap(final String className) {
    ClassInfo info = mock(ClassInfo.class);
    when(info.getName()).thenReturn(className);
    return info;
  }
}
