package org.robolectric.shadows;

import android.preference.PreferenceCategory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricBase;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceCategoryTest {

  private PreferenceCategory category;
  private ShadowPreferenceCategory shadow;

  @Before
  public void setUp() throws Exception {
    category = new PreferenceCategory(Robolectric.application);
    shadow = RobolectricBase.shadowOf(category);
  }

  @Test
  public void shouldInheritFromPreferenceGroup() {
    assertThat(shadow).isInstanceOf(ShadowPreferenceGroup.class);
  }
}
