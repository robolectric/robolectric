package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link FragmentScenario} in Robolectric. */
@RunWith(AndroidJUnit4.class)
public class FragmentScenarioTest {

  @Test
  public void launchFragment() {
    final AtomicReference<Fragment> loadedFragment = new AtomicReference<>();
    FragmentScenario.launch(Fragment.class).onFragment(loadedFragment::set);
    assertThat(loadedFragment.get()).isNotNull();
  }

  /**
   * This is a stress test to see if Robolectric instrumentation supports Kotlin-compiled bytecode.
   * There have been some issues in the past with Robolectric instrumentation running on AndroidX
   * code, particularly constructors. If this test breaks, please add `@Ignore` and file an issue.
   */
  @Test
  @Config(instrumentedPackages = "androidx.")
  public void launchFragmentInstrumented() {
    final AtomicReference<Fragment> loadedFragment = new AtomicReference<>();
    FragmentScenario.launch(Fragment.class).onFragment(loadedFragment::set);
    assertThat(loadedFragment.get()).isNotNull();
  }
}
