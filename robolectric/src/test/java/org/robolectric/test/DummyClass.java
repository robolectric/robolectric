package org.robolectric.test;

import org.robolectric.internal.bytecode.SandboxClassLoader;

/**
 * Dummy class placed in package that is not loaded by parent classloader of {@link SandboxClassLoader}.
 * @see org.robolectric.RobolectricTestRunnerClassLoaderConfigTest#testGetPackage()
 */
public class DummyClass {

  // nothing here

}
