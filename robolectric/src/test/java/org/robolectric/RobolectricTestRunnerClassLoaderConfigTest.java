package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.test.DummyClass;

@RunWith(AndroidJUnit4.class)
public class RobolectricTestRunnerClassLoaderConfigTest {

  @Test
  public void testUsingClassLoader() throws ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Assert.assertEquals(classLoader.getClass().getName(), SandboxClassLoader.class.getName());
  }

  @Test
  public void testGetPackage() {
    assertThat(DummyClass.class.getClassLoader()).isInstanceOf(SandboxClassLoader.class);
    assertThat(DummyClass.class.getPackage()).isNotNull();
    assertThat(DummyClass.class.getName()).startsWith(DummyClass.class.getPackage().getName());
  }

  @Test public void testPackagesFromParentClassLoaderAreMadeAvailableByName() throws Exception {
    assertThat(Test.class.getPackage()).isNotNull();
    assertThat(Package.getPackage("org.junit")).isNotNull();
    assertThat(Package.getPackage("org.junit")).isEqualTo(Test.class.getPackage());
  }
}
