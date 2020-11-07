package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;

import android.app.Instrumentation;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** {@link InstrumentationRegistry} tests. */
@RunWith(AndroidJUnit4.class)
public class InstrumentationRegistryTest {

  private static Instrumentation priorInstrumentation = null;
  private static Context priorContext = null;

  @Test
  public void getArguments() {
    assertThat(InstrumentationRegistry.getArguments()).isNotNull();
  }

  @Test
  public void getInstrumentation() {
    assertThat(InstrumentationRegistry.getInstrumentation()).isNotNull();
  }

  @Test
  public void getTargetContext() {
    assertThat(InstrumentationRegistry.getTargetContext()).isNotNull();
    assertThat(InstrumentationRegistry.getContext()).isNotNull();
  }

  @Test
  public void uniqueInstancesPerTest() {
    checkInstances();
  }

  @Test
  public void uniqueInstancesPerTest2() {
    checkInstances();
  }

  /**
   * Verifies that each test gets a new Instrumentation and Context, by comparing against instances
   * stored by prior test.
   */
  private void checkInstances() {
    if (priorInstrumentation == null) {
      priorInstrumentation = InstrumentationRegistry.getInstrumentation();
      priorContext = InstrumentationRegistry.getTargetContext();
    } else {
      assertThat(priorInstrumentation).isNotEqualTo(InstrumentationRegistry.getInstrumentation());
      assertThat(priorContext).isNotEqualTo(InstrumentationRegistry.getTargetContext());
    }
  }

}
