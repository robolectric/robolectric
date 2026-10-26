package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Existing JUnit 4 tests keep working, and run in the same sandboxes as the Jupiter tests. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.NEWEST_SDK)
public class JUnit4RobolectricTest {
  @Test
  public void runsWithRobolectric() {
    assertThat(Build.VERSION.SDK_INT).isAtLeast(Build.VERSION_CODES.M);
    assertThat((Application) ApplicationProvider.getApplicationContext()).isNotNull();
  }
}
