package org.robolectric.bytecode;

import org.junit.Before;

import java.net.URL;

public class PreInstrumentedClassLoaderTest extends InstrumentingClassLoaderTestBase {

  @Before public void setup() {
    // No need to write out coverage data.
    System.setProperty("jacoco-agent.output", "none");
  }

  protected ClassLoader createClassLoader(Setup setup) throws ClassNotFoundException {
    URL instrumentedClasses = getClass().getClassLoader().getResource("instrumented.jar");
    return new AsmInstrumentingClassLoader(setup, instrumentedClasses);
  }
}
