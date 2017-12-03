package org.robolectric.shadows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.widget.NumberPicker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowNumberPickerTest {

  @Test
  public void shouldFireListeners() {
    NumberPicker picker = new NumberPicker(RuntimeEnvironment.application);

    NumberPicker.OnValueChangeListener listener = mock(NumberPicker.OnValueChangeListener.class);
    picker.setOnValueChangedListener(listener);

    ShadowNumberPicker shadowNumberPicker = Shadows.shadowOf(picker);
    shadowNumberPicker.getOnValueChangeListener().onValueChange(picker, 5, 10);

    verify(listener).onValueChange(picker, 5, 10);
  }
}
