package org.robolectric.shadows;

import android.widget.Switch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class SwitchTest {
  @Test
  public void clickingToggles() {
    Switch theSwitch = new Switch(Robolectric.application);
    assertThat(theSwitch.isChecked()).isFalse();

    theSwitch.performClick();
    assertThat(theSwitch.isChecked()).isTrue();

    theSwitch.performClick();
    assertThat(theSwitch.isChecked()).isFalse();
  }
}
