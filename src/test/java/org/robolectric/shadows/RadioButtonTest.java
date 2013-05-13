package org.robolectric.shadows;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class RadioButtonTest {
  @Test
  public void canBeExplicitlyChecked() throws Exception {
    RadioButton radioButton = new RadioButton(Robolectric.application);
    assertFalse(radioButton.isChecked());

    radioButton.setChecked(true);
    assertTrue(radioButton.isChecked());

    radioButton.setChecked(false);
    assertFalse(radioButton.isChecked());
  }

  @Test
  public void canBeToggledBetweenCheckedState() throws Exception {
    RadioButton radioButton = new RadioButton(Robolectric.application);
    assertFalse(radioButton.isChecked());

    radioButton.toggle();
    assertTrue(radioButton.isChecked());

    radioButton.toggle();
    assertTrue(radioButton.isChecked()); // radio buttons can't be turned off again with a click
  }

  @Test
  public void canBeClickedToToggleCheckedState() throws Exception {
    RadioButton radioButton = new RadioButton(Robolectric.application);
    assertFalse(radioButton.isChecked());

    radioButton.performClick();
    assertTrue(radioButton.isChecked());

    radioButton.performClick();
    assertTrue(radioButton.isChecked()); // radio buttons can't be turned off again with a click
  }

  @Test
  public void shouldInformRadioGroupThatItIsChecked() throws Exception {
    RadioButton radioButton1 = new RadioButton(Robolectric.application);
    radioButton1.setId(99);
    RadioButton radioButton2 = new RadioButton(Robolectric.application);
    radioButton2.setId(100);

    RadioGroup radioGroup = new RadioGroup(Robolectric.application);
    radioGroup.addView(radioButton1);
    radioGroup.addView(radioButton2);

    radioButton1.setChecked(true);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(radioButton1.getId());

    radioButton2.setChecked(true);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(radioButton2.getId());
  }
}
