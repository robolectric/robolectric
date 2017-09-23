package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowDatePickerDialogTest {

  @Test
  public void returnsTheInitialYearMonthAndDayPassedIntoTheDatePickerDialog() throws Exception {
    Locale.setDefault(Locale.US);
    DatePickerDialog datePickerDialog = new DatePickerDialog(RuntimeEnvironment.application, null, 2012, 6, 7);
    assertThat(shadowOf(datePickerDialog).getYear()).isEqualTo(2012);
    assertThat(shadowOf(datePickerDialog).getMonthOfYear()).isEqualTo(6);
    assertThat(shadowOf(datePickerDialog).getDayOfMonth()).isEqualTo(7);
  }

  @Test
  public void savesTheCallback() {
    DatePickerDialog.OnDateSetListener expectedDateSetListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        // ignored
      }
    };

    DatePickerDialog datePickerDialog = new DatePickerDialog(RuntimeEnvironment.application, expectedDateSetListener, 2012, 6, 7);

    ShadowDatePickerDialog shadowDatePickerDialog = shadowOf(datePickerDialog);
    assertThat(shadowDatePickerDialog.getOnDateSetListenerCallback()).isEqualTo(expectedDateSetListener);
  }
}
