package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.TimePickerDialog;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
public class ShadowTimePickerDialogTest {

  @Test
  public void returnsTheIntialHourAndMinutePassedIntoTheTimePickerDialog() throws Exception {
    TimePickerDialog timePickerDialog = new TimePickerDialog(RuntimeEnvironment.application, 0, null, 6, 55, false);
    ShadowTimePickerDialog shadow = shadowOf(timePickerDialog);
    assertThat(shadow.getHourOfDay()).isEqualTo(6);
    assertThat(shadow.getMinute()).isEqualTo(55);
  }
}
