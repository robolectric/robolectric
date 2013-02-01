package org.robolectric.bytecode;

public class AsmInstrumentingClassLoaderTest extends InstrumentingClassLoaderTest {
    protected ClassLoader createClassLoader(Setup setup) throws ClassNotFoundException {
        return new AsmInstrumentingClassLoader(setup, getClass().getClassLoader());
    }
}
