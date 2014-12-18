package org.robolectric.test;

import org.robolectric.RobolectricTestRunnerClassLoaderSetupTest;
import org.robolectric.internal.bytecode.AsmInstrumentingClassLoader;

/**
 * Dummy class placed in package that is not loaded by parent classloader of {@link AsmInstrumentingClassLoader}.
 * @see RobolectricTestRunnerClassLoaderSetupTest#testGetPackage()
 */
public class DummyClass {

  // nothing here

}
