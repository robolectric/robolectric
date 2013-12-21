package org.robolectric.shadows;

import android.widget.NumberPicker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.fail;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class NumberPickerTest {

  private NumberPicker numberPicker;

  @Before
  public void setUp() throws Exception {
    numberPicker = new NumberPicker(Robolectric.application);
  }

  @Test
  public void setDisplayedValues_shouldCheckArraySize() throws Exception {
    numberPicker.setMaxValue(2);

    try {
      numberPicker.setDisplayedValues(new String[]{"0", "1"});
      fail("should have complained about being too small");
    } catch (Exception e) {
      // pass
    }

    numberPicker.setDisplayedValues(new String[]{"0", "1", "2"});
    // ahhh, just right

    try {
      numberPicker.setDisplayedValues(new String[]{"0", "1", "2", "3"});
      fail("should have complained about being too big");
    } catch (Exception e) {
      // pass
    }
  }

  @Test
  public void getSetMinValue() throws Exception {
    assertThat(numberPicker.getMinValue()).isEqualTo(0);
    numberPicker.setMinValue(5);
    assertThat(numberPicker.getMinValue()).isEqualTo(5);
  }

  @Test
  public void getSetMaxValue() throws Exception {
    assertThat(numberPicker.getMaxValue()).isEqualTo(0);
    numberPicker.setMaxValue(6);
    assertThat(numberPicker.getMaxValue()).isEqualTo(6);
  }
}
