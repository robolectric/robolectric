package com.xtremelabs.robolectric.shadows;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class RadioButtonTest {
    @Test
    public void canBeExplicitlyChecked() throws Exception {
        RadioButton radioButton = new RadioButton(null);
        assertFalse(radioButton.isChecked());

        radioButton.setChecked(true);
        assertTrue(radioButton.isChecked());

        radioButton.setChecked(false);
        assertFalse(radioButton.isChecked());
    }

    @Test
    public void canBeToggledBetweenCheckedState() throws Exception {
        RadioButton radioButton = new RadioButton(null);
        assertFalse(radioButton.isChecked());

        radioButton.toggle();
        assertTrue(radioButton.isChecked());

        radioButton.toggle();
        assertFalse(radioButton.isChecked());
    }

    @Test
    public void canBeClickedToToggleCheckedState() throws Exception {
        RadioButton radioButton = new RadioButton(null);
        assertFalse(radioButton.isChecked());

        radioButton.performClick();
        assertTrue(radioButton.isChecked());

        radioButton.performClick();
        assertFalse(radioButton.isChecked());
    }

    @Test
    public void shouldInformRadioGroupThatItIsChecked() throws Exception {
        RadioButton radioButton1 = new RadioButton(null);
        radioButton1.setId(99);
        RadioButton radioButton2 = new RadioButton(null);
        radioButton2.setId(100);

        RadioGroup radioGroup = new RadioGroup(null);
        radioGroup.addView(radioButton1);
        radioGroup.addView(radioButton2);

        radioButton1.setChecked(true);
        assertThat(radioGroup.getCheckedRadioButtonId(), equalTo(radioButton1.getId()));

        radioButton2.setChecked(true);
        assertThat(radioGroup.getCheckedRadioButtonId(), equalTo(radioButton2.getId()));
    }
}
