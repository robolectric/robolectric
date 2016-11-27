package org.robolectric;

import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import static org.assertj.core.api.Assertions.assertThat;

public class ResetterTest {
  @Test
  public void testReset() throws Exception {
    RobolectricTestRunner runner = new RobolectricTestRunner(TestClass.class);
    RealShadow.testStatus = true;
    runner.resetUserShadows(runner.getConfig(TestClass.class.getMethod("test")));
    assertThat(RealShadow.testStatus).isFalse();
  }

  private class Real {
  }

  @Implements(ResetterTest.Real.class)
  static class RealShadow {
    static boolean testStatus;

    @Resetter
    public static void reset() {
      testStatus = false;
    }
  }

  @Config(sdk = 1, manifest = "foo", application = TestFakeApp.class, packageName = "com.example.test", shadows = RealShadow.class, instrumentedPackages = "com.example.test1", libraries = "libs/test", qualifiers = "from-test", resourceDir = "test/res", assetDir = "test/assets", constants = RobolectricTestRunnerTest.BuildConfigConstants.class)
  public static class TestClass {
    @Test
    public void test() {
    }
  }
}
