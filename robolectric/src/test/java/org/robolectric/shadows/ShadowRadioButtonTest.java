package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.drawable.ColorDrawable;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowRadioButtonTest {
  @Test
  public void canBeExplicitlyChecked() throws Exception {
    RadioButton radioButton = new RadioButton(RuntimeEnvironment.application);
    assertFalse(radioButton.isChecked());

    radioButton.setChecked(true);
    assertTrue(radioButton.isChecked());

    radioButton.setChecked(false);
    assertFalse(radioButton.isChecked());
  }

  @Test
  public void canBeToggledBetweenCheckedState() throws Exception {
    RadioButton radioButton = new RadioButton(RuntimeEnvironment.application);
    assertFalse(radioButton.isChecked());

    radioButton.toggle();
    assertTrue(radioButton.isChecked());

    radioButton.toggle();
    assertTrue(radioButton.isChecked()); // radio buttons can't be turned off again with a click
  }

  @Test
  public void canBeClickedToToggleCheckedState() throws Exception {
    RadioButton radioButton = new RadioButton(RuntimeEnvironment.application);
    assertFalse(radioButton.isChecked());

    radioButton.performClick();
    assertTrue(radioButton.isChecked());

    radioButton.performClick();
    assertTrue(radioButton.isChecked()); // radio buttons can't be turned off again with a click
  }

  @Test
  public void shouldInformRadioGroupThatItIsChecked() throws Exception {
    RadioButton radioButton1 = new RadioButton(RuntimeEnvironment.application);
    radioButton1.setId(99);
    RadioButton radioButton2 = new RadioButton(RuntimeEnvironment.application);
    radioButton2.setId(100);

    RadioGroup radioGroup = new RadioGroup(RuntimeEnvironment.application);
    radioGroup.addView(radioButton1);
    radioGroup.addView(radioButton2);

    radioButton1.setChecked(true);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(radioButton1.getId());

    radioButton2.setChecked(true);
    assertThat(radioGroup.getCheckedRadioButtonId()).isEqualTo(radioButton2.getId());
  }

  @Test
  public void getButtonDrawableId() {
    RadioButton radioButton = new RadioButton(RuntimeEnvironment.application);

    radioButton.setButtonDrawable(R.drawable.an_image);
    assertThat(shadowOf(radioButton).getButtonDrawableId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void getButtonDrawable() {
    RadioButton radioButton = new RadioButton(RuntimeEnvironment.application);

    ColorDrawable drawable = new ColorDrawable();
    radioButton.setButtonDrawable(drawable);
    assertThat(shadowOf(radioButton).getButtonDrawable()).isEqualTo(drawable);
  }
}
