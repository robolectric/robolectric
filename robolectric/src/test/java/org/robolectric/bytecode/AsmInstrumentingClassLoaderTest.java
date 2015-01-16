package org.robolectric.bytecode;

import org.junit.Test;
import org.robolectric.util.Transcript;

public class AsmInstrumentingClassLoaderTest extends InstrumentingClassLoaderTestBase {
  protected ClassLoader createClassLoader(Setup setup) throws ClassNotFoundException {
    return new AsmInstrumentingClassLoader(setup);
  }

  @Test public void shouldCacheMisses() throws Exception {
    final Transcript transcript = new Transcript();

    AsmInstrumentingClassLoader classLoader = new AsmInstrumentingClassLoader(new Setup()) {
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
