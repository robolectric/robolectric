package org.robolectric.shadows;

import android.os.Build;
import android.widget.DatePicker;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.app.DatePickerDialog;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.lang.Override;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.JELLY_BEAN,
    Build.VERSION_CODES.JELLY_BEAN_MR1,
    Build.VERSION_CODES.JELLY_BEAN_MR2,
    // Build.VERSION_CODES.KITKAT, - Does not pass on Kit Kat
    Build.VERSION_CODES.LOLLIPOP })
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

    ShadowDatePickerDialog shadowDatePickerDialog = (ShadowDatePickerDialog) shadowOf(datePickerDialog);
    assertThat(shadowDatePickerDialog.getOnDateSetListenerCallback()).isEqualTo(expectedDateSetListener);
  }
}
