package org.robolectric.bytecode;

public class AsmInstrumentingClassLoaderTest extends InstrumentingClassLoaderTestBase {
    protected ClassLoader createClassLoader(Setup setup) throws ClassNotFoundException {
        return new AsmInstrumentingClassLoader(setup);
    }
}
