package org.robolectric.internal.bytecode;

import android.os.Build;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.internal.AndroidConfigurer;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

public class AndroidInstrumentingClassLoaderTest {

  private ClassLoader classLoader;

  @Before
  public void setUp() throws Exception {
    classLoader = new InstrumentingClassLoader(configureBuilder().build());
  }

  @Test
  public void shouldMakeBuildVersionIntsNonFinal() throws Exception {
    Class<?> versionClass = loadClass(Build.VERSION.class);
    int modifiers = versionClass.getDeclaredField("SDK_INT").getModifiers();
    assertThat(Modifier.isFinal(modifiers)).as("SDK_INT should be non-final").isFalse();
  }

  ////////////////////////

  @NotNull
  private InstrumentationConfiguration.Builder configureBuilder() {
    return AndroidConfigurer.configure(InstrumentationConfiguration.newBuilder(), new AndroidInterceptors().build());
  }

  private Class<?> loadClass(Class<?> clazz) throws ClassNotFoundException {
    return classLoader.loadClass(clazz.getName());
  }
}
