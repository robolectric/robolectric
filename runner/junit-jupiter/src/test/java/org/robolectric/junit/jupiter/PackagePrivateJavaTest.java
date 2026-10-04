package org.robolectric.junit.jupiter;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robolectric.annotation.Config;

/** Tests that a test class and its methods don't have to be public, as is usual in Java. */
@ExtendWith(RobolectricExtension.class)
@Config(sdk = 34)
class PackagePrivateJavaTest {
  private boolean setUpRan;

  @BeforeEach
  void setUp() {
    setUpRan = true;
  }

  @Test
  void runsInTheAndroidEnvironment() {
    assertThat(setUpRan).isTrue();
    assertThat(Build.VERSION.SDK_INT).isEqualTo(34);
  }
}
