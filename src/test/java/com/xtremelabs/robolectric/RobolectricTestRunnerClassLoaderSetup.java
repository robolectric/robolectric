package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(WithTestDefaultsRunner.class)
public class RobolectricTestRunnerClassLoaderSetup {

    @Test
    // TODO this test fails in IntelliJ
    public void testUsingClassLoader() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assert.assertEquals(classLoader.getClass().getName(), RobolectricClassLoader.class.getName());
    }
}
