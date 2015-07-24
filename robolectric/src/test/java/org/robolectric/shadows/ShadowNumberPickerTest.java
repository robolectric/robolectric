package org.robolectric.shadows;

import android.os.Build;
import android.widget.NumberPicker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.JELLY_BEAN,
    Build.VERSION_CODES.JELLY_BEAN_MR1,
    Build.VERSION_CODES.JELLY_BEAN_MR2,
    Build.VERSION_CODES.KITKAT})
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
