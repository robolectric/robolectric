package org.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.bytecode.RobolectricClassLoader;


@RunWith(TestRunners.WithDefaults.class)
public class RobolectricTestRunnerClassLoaderSetup {

    @Test
    public void testUsingClassLoader() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertEquals(classLoader.getClass().getName(), RobolectricClassLoader.class.getName());
    }
}
