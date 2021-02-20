package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.DatePickerDialog;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowDatePickerDialogTest {

  @Test
  public void returnsTheInitialYearMonthAndDayPassedIntoTheDatePickerDialog() {
    Locale.setDefault(Locale.US);
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(ApplicationProvider.getApplicationContext(), null, 2012, 6, 7);
    assertThat(shadowOf(datePickerDialog).getYear()).isEqualTo(2012);
    assertThat(shadowOf(datePickerDialog).getMonthOfYear()).isEqualTo(6);
    assertThat(shadowOf(datePickerDialog).getDayOfMonth()).isEqualTo(7);
  }

  @Test
  public void savesTheCallback() {
    DatePickerDialog.OnDateSetListener expectedDateSetListener =
        (datePicker, i, i1, i2) -> {
          // ignored
        };

    DatePickerDialog datePickerDialog =
        new DatePickerDialog(
            ApplicationProvider.getApplicationContext(), expectedDateSetListener, 2012, 6, 7);

    ShadowDatePickerDialog shadowDatePickerDialog = shadowOf(datePickerDialog);
    assertThat(shadowDatePickerDialog.getOnDateSetListenerCallback()).isEqualTo(expectedDateSetListener);
  }
}
