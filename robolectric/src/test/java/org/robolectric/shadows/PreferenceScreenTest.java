package org.robolectric.shadows;

import android.app.Dialog;
import android.preference.PreferenceScreen;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceScreenTest {

  private PreferenceScreen screen;
  private ShadowPreferenceScreen shadow;

  @Before
  public void setUp() throws Exception {
    screen = Shadow.newInstanceOf(PreferenceScreen.class);
    shadow = Shadows.shadowOf(screen);
  }

  @Test
  public void shouldInheritFromPreferenceGroup() {
    assertThat(shadow).isInstanceOf(ShadowPreferenceGroup.class);
  }

  @Test
  public void shouldSetDialog() {
    Dialog dialog = new Dialog(RuntimeEnvironment.application);

    assertThat(screen.getDialog()).isNull();
    shadow.setDialog(dialog);
    assertThat(screen.getDialog()).isSameAs(dialog);
  }
}
