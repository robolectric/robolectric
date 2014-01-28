package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import android.widget.NumberPicker;
import org.robolectric.TestRunners;
import org.robolectric.Robolectric;
import static junit.framework.Assert.*;

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
}
