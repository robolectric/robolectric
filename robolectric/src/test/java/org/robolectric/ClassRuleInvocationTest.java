package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import android.os.Build;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions;

@RunWith(RobolectricTestRunner.class)
public final class ClassRuleInvocationTest {
  private static final AtomicReference<TemporaryFolder> tempFolder = new AtomicReference<>();
  private static final int CUR_SDK = Build.VERSION.SDK_INT;
  private static final AtomicInteger counter = new AtomicInteger(0);

  @ClassRule
  public static final TestRule TEMP_FOLDER_RULE =
      new TestRule() {
        @Override
        public Statement apply(Statement base, Description description) {
          return new Statement() {
            @Override
            public void evaluate() throws Throwable {
              // Set the tempFolder
              tempFolder.set(new TemporaryFolder());
              base.evaluate();
            }
          };
        }
      };

  @ClassRule public static final SdkCheckClassRule sdkCheckClassRule = new SdkCheckClassRule();

  public static class SdkCheckClassRule implements TestRule {
    public SdkCheckClassRule() {
      // Check that Android code can be executed when the rule is constructed.
      assertThat(Build.VERSION.SDK_INT).isEqualTo(CUR_SDK);
      assertThat(counter.incrementAndGet()).isEqualTo(1);
    }

    @Override
    public Statement apply(Statement base, Description description) {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(CUR_SDK);
      assertThat(counter.incrementAndGet()).isEqualTo(2);

      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          assertThat(Build.VERSION.SDK_INT).isEqualTo(CUR_SDK);
          assertThat(counter.incrementAndGet()).isEqualTo(3);
          base.evaluate();
        }
      };
    }
  }

  @Test
  public void comparisonCount() {
    assertThat(counter.get()).isEqualTo(3);
  }

  @Test
  public void testUsingTempFolder() throws IOException {
    assertNotNull(tempFolder.get());
  }

  // Make sure that the ClassRule is setup for other sandboxes.
  @Test
  @Config(sdk = AndroidVersions.S.SDK_INT)
  public void anotherSandboxTempFolder() throws IOException {
    assertNotNull(tempFolder.get());
  }

  @Test
  @Config(sdk = AndroidVersions.S.SDK_INT)
  public void anotherSandboxComparisonCount() throws IOException {
    assertThat(counter.get()).isEqualTo(3);
  }
}
