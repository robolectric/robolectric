package org.robolectric.internal.bytecode;

import org.junit.Test;
import org.robolectric.util.Transcript;

public class InstrumentingClassLoaderTest extends InstrumentingClassLoaderTestBase {
  protected ClassLoader createClassLoader(InstrumentingClassLoaderConfig config) throws ClassNotFoundException {
    return new InstrumentingClassLoader(config);
  }

  @Test public void shouldCacheMisses() throws Exception {
    final Transcript transcript = new Transcript();

    InstrumentingClassLoader classLoader = new InstrumentingClassLoader(new InstrumentingClassLoaderConfig()) {
      @Override
      protected Class<?> findClass(String className) throws ClassNotFoundException {
        transcript.add("find " + className);
        throw new ClassNotFoundException(className);
      }
    };

    try {
      classLoader.loadClass("foo.AClass");
    } catch (ClassNotFoundException e) {
      // expected
    }
    try {
      classLoader.loadClass("foo.AClass");
    } catch (ClassNotFoundException e) {
      // expected
    }

    transcript.assertEventsSoFar("find foo.AClass");
  }
}
