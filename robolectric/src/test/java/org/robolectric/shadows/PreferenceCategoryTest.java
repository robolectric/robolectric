package org.robolectric.shadows;

import android.preference.PreferenceCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceCategoryTest {
  private ShadowPreferenceCategory shadow;

  @Before
  public void setUp() throws Exception {
    PreferenceCategory category = new PreferenceCategory(RuntimeEnvironment.application);
    shadow = Shadows.shadowOf(category);
  }

  @Test
  public void shouldInheritFromPreferenceGroup() {
    assertThat(shadow).isInstanceOf(ShadowPreferenceGroup.class);
  }
}
