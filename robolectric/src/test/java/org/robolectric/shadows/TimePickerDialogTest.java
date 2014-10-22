package org.robolectric.shadows;

import android.app.TimePickerDialog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class TimePickerDialogTest {

  @Test
  public void returnsTheIntialHourAndMinutePassedIntoTheTimePickerDialog() throws Exception {
    TimePickerDialog timePickerDialog = new TimePickerDialog(Robolectric.application, 0, null, 6, 55, false);
    ShadowTimePickerDialog shadow = shadowOf(timePickerDialog);
    assertThat(shadow.getHourOfDay()).isEqualTo(6);
    assertThat(shadow.getMinute()).isEqualTo(55);
  }
}
