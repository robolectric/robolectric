package org.robolectric.shadows;

import android.widget.ToggleButton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ToggleButtonTest {

  @Test
  public void clickingToggles() {
    ToggleButton toggleButton = new ToggleButton(Robolectric.application);
    assertThat(toggleButton.isChecked()).isFalse();

    toggleButton.performClick();
    assertThat(toggleButton.isChecked()).isTrue();

    toggleButton.performClick();
    assertThat(toggleButton.isChecked()).isFalse();
  }
}
