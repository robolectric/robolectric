package org.robolectric.shadows;

import android.widget.NumberPicker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(TestRunners.WithDefaults.class)
public class NumberPickerTest {

  @Test
  public void setDisplayedValues_shouldCheckArraySize() throws Exception {
    NumberPicker picker = new NumberPicker(Robolectric.application);
    picker.setMaxValue(2);
    picker.setDisplayedValues(null);

    try {
      picker.setDisplayedValues(new String[] {"0", "1"});
      fail("should have complained about being too small");
    } catch (Exception e) {
      // pass
    }

    picker.setDisplayedValues(new String[] {"0", "1", "2"});

    try {
      picker.setDisplayedValues(new String[] {"0", "1", "2", "3"});
      fail("should have complained about being too big");
    } catch (Exception e) {
      // pass
    }
  }

  @Test
  public void shouldFireListeners() {
    NumberPicker picker = new NumberPicker(Robolectric.application);

    NumberPicker.OnValueChangeListener listener = mock(NumberPicker.OnValueChangeListener.class);
    picker.setOnValueChangedListener(listener);

    ShadowNumberPicker shadowNumberPicker = Robolectric.shadowOf(picker);
    shadowNumberPicker.getOnValueChangeListener().onValueChange(picker, 5, 10);

    verify(listener).onValueChange(picker, 5, 10);
  }
}
