package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link FragmentScenario} in Robolectric. */
@RunWith(AndroidJUnit4.class)
public class FragmentScenarioTest {

  @Test
  public void launchFragment() {
    final AtomicReference<Fragment> loadedFragment = new AtomicReference<>();
    FragmentScenario.launch(Fragment.class).onFragment(loadedFragment::set);
    assertThat(loadedFragment.get()).isNotNull();
  }
}
