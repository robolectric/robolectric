package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import android.app.DatePickerDialog;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import java.util.Locale;

import static org.robolectric.Robolectric.*;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class DatePickerDialogTest {

  @Test
  public void returnsTheInitialYearMonthAndDayPassedIntoTheDatePickerDialog() throws Exception {
    Locale.setDefault(Locale.US);
    DatePickerDialog datePickerDialog = new DatePickerDialog(Robolectric.application, null, 2012, 6, 7);
    assertThat(shadowOf(datePickerDialog).getYear()).isEqualTo(2012);
    assertThat(shadowOf(datePickerDialog).getMonthOfYear()).isEqualTo(6);
    assertThat(shadowOf(datePickerDialog).getDayOfMonth()).isEqualTo(7);
  }
}
