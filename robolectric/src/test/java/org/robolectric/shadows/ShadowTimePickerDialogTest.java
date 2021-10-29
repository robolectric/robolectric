package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowTimePickerDialogTest {

  @Test
  public void testGettersReturnInitialConstructorValues() {
    TimePickerDialog timePickerDialog =
        new TimePickerDialog(ApplicationProvider.getApplicationContext(), 0, null, 6, 55, true);
    ShadowTimePickerDialog shadow = shadowOf(timePickerDialog);
    assertThat(shadow.getHourOfDay()).isEqualTo(6);
    assertThat(shadow.getMinute()).isEqualTo(55);
    assertThat(shadow.getIs24HourView()).isEqualTo(true);
  }

  @Test
  public void updateTime_shouldUpdateHourAndMinute() {
    TimePickerDialog timePickerDialog =
        new TimePickerDialog(ApplicationProvider.getApplicationContext(), 0, null, 6, 55, true);
    timePickerDialog.updateTime(1, 2);

    Bundle bundle = timePickerDialog.onSaveInstanceState();
    assertThat(bundle.getInt("hour")).isEqualTo(1);
    assertThat(bundle.getInt("minute")).isEqualTo(2);
  }
}
