package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;

@RunWith(JUnit4.class)
public class InstrumentationConfigurationTest {
  private InstrumentationConfiguration config;

  @Before
  public void setUp() throws Exception {
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
    new AndroidConfigurer(new ShadowProviders(Collections.emptyList()))
        .configure(builder, new Interceptors(AndroidInterceptors.all()));
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
    assertThat(config.shouldInstrument(wrap("android.and.now.for.something.completely.different")))
        .isTrue();
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
  public void shouldAcquireAndroidRClasses() throws Exception {
    assertThat(config.shouldAcquire("android.Rfoo")).isTrue();
    assertThat(config.shouldAcquire("android.fooR")).isTrue();
    assertThat(config.shouldAcquire("android.R")).isTrue();
    assertThat(config.shouldAcquire("android.R$anything")).isTrue();
    assertThat(config.shouldAcquire("android.R$anything$else")).isTrue();
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
    assertThat(config.shouldAcquire("com.almworks.sqlite4java.whatever.Cls$anything$else"))
        .isFalse();
  }

  @Test
  public void shouldNotAcquireShadowClass() throws Exception {
    assertThat(config.shouldAcquire("org.robolectric.shadow.api.Shadow")).isTrue();
  }

  @Test
  public void shouldAcquireDistinguishedNameParser_Issue1864() throws Exception {
    assertThat(config.shouldAcquire("javax.net.ssl.DistinguishedNameParser")).isTrue();
  }

  @Test
  public void shouldAcquireOpenglesGL_Issue2960() throws Exception {
    assertThat(config.shouldAcquire("javax.microedition.khronos.opengles.GL")).isTrue();
  }

  @Test
  public void shouldInstrumentCustomClasses() throws Exception {
    String instrumentName = "com.whatever.SomeClassNameToInstrument";
    String notInstrumentName = "com.whatever.DoNotInstrumentMe";
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder().addInstrumentedClass(instrumentName).build();
    assertThat(customConfig.shouldInstrument(wrap(instrumentName))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap(notInstrumentName))).isFalse();
  }

  @Test
  public void equals_ShouldCheckClassNames() throws Exception {
    String instrumentName = "com.whatever.SomeClassNameToInstrument";
    InstrumentationConfiguration baseConfig = InstrumentationConfiguration.newBuilder().build();
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder().addInstrumentedClass(instrumentName).build();

    assertThat(baseConfig).isNotEqualTo(customConfig);
  }

  @Test
  public void shouldNotInstrumentListedClasses() throws Exception {
    String instrumentName = "android.foo.bar";
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder().doNotInstrumentClass(instrumentName).build();

    assertThat(customConfig.shouldInstrument(wrap(instrumentName))).isFalse();
  }

  @Test
  public void shouldNotInstrumentPackages() throws Exception {
    String includedClass = "android.foo.Bar";
    String excludedClass = "androidx.test.foo.Bar";
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("android.")
            .doNotInstrumentPackage("androidx.test.")
            .build();

    assertThat(customConfig.shouldInstrument(wrap(includedClass))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap(excludedClass))).isFalse();
  }

  @Test
  public void shouldNotInstrumentClassNamesWithNullRegex() throws Exception {
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("com.random")
            .setDoNotInstrumentClassRegex(null)
            .build();

    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass"))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass_Delegate"))).isTrue();
  }

  @Test
  public void shouldNotInstrumentClassNamesWithRegex() throws Exception {
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("com.random")
            .setDoNotInstrumentClassRegex(".*_Delegate")
            .build();

    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass"))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass_Delegate"))).isFalse();
  }

  @Test
  public void shouldNotInstrumentClassNamesWithMultiRegex() throws Exception {
    InstrumentationConfiguration customConfig =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("com.random")
            .setDoNotInstrumentClassRegex(".*_Delegate|.*_BadThings|com\\.random\\.badpackage.*")
            .build();

    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass"))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass_Delegate"))).isFalse();
    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass_BadThings"))).isFalse();
    assertThat(customConfig.shouldInstrument(wrap("com.random.testclass_GoodThings"))).isTrue();
    assertThat(customConfig.shouldInstrument(wrap("com.random.badpackage.testclass"))).isFalse();
    assertThat(customConfig.shouldInstrument(wrap("com.random.goodpackage.testclass"))).isTrue();
  }

  @Test
  public void removesRedundantPackageAndClassConfig() throws Exception {
    InstrumentationConfiguration config1 =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("a.b")
            .addInstrumentedPackage("a.")
            .addInstrumentedPackage("a.c")
            .addInstrumentedClass("a.SomeClass")
            .addInstrumentedClass("a.b.SomeClass")
            .build();

    InstrumentationConfiguration config2 =
        InstrumentationConfiguration.newBuilder()
            .addInstrumentedPackage("a.x")
            .addInstrumentedPackage("a.")
            .addInstrumentedPackage("a.y")
            .addInstrumentedClass("a.c.AnotherClass")
            .addInstrumentedClass("a.d.AnotherClass")
            .build();

    // The builder should remove redundant config, and both configs instrument the 'a.' package.
    assertThat(config1).isEqualTo(config2);
  }

  private static ClassDetails wrap(final String className) {
    ClassDetails info = mock(ClassDetails.class);
    when(info.getName()).thenReturn(className);
    return info;
  }
}
