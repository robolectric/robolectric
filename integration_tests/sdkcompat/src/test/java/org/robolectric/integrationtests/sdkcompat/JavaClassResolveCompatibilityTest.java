package org.robolectric.integrationtests.sdkcompat;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

/**
 * Test class for Java's class resolve compatibility test. We must keep it with Java instead of
 * converting it to Kotlin, because Kotlin has different behavior than Java without any error.
 */
@RunWith(RobolectricTestRunner.class)
public class JavaClassResolveCompatibilityTest {
  @Test
  public void sdkIs29() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.Q);
  }

  @Test
  public void shadowOf() {
    // https://github.com/robolectric/robolectric/issues/7095
    assertThat(Shadows.shadowOf(RuntimeEnvironment.getApplication())).isNotNull();
  }
}
