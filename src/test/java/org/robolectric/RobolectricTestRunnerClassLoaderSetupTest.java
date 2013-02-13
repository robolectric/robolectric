package org.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.bytecode.AsmInstrumentingClassLoader;


@RunWith(TestRunners.WithDefaults.class)
public class RobolectricTestRunnerClassLoaderSetupTest {

    @Test
    public void testUsingClassLoader() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertEquals(classLoader.getClass().getName(), AsmInstrumentingClassLoader.class.getName());
    }
}
