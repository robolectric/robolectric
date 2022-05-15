package org.robolectric.integration.compat.target28;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Test class for Java's class resolve compatibility test. We must keep it with Java instead of
 * converting it to Kotlin, because Kotlin has different behavior than Java without any error.
 */
@RunWith(RobolectricTestRunner.class)
public class JavaClassResolveCompatibilityTest {
  @Test
  public void sdkIs28() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(Build.VERSION_CODES.P);
  }

  @Test
  public void shadowOf() {
    // https://github.com/robolectric/robolectric/issues/7095
    // Enable this assertion when resolving all shadowOf compatibility problem
    // assertThat(Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()))
    //     .isNotNull();
  }
}
