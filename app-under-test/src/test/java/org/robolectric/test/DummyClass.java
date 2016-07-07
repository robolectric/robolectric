package org.robolectric.test;

/**
 * Dummy class placed in package that is not loaded by parent classloader of {@link org.robolectric.internal.bytecode.InstrumentingClassLoader}.
 * @see org.robolectric.RobolectricTestRunnerClassLoaderConfigTest#testGetPackage()
 */
public class DummyClass {

  // nothing here

}
