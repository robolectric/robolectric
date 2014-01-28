package org.robolectric.shadows;

import android.app.DatePickerDialog;
import junit.framework.TestCase;
import libcore.icu.LocaleData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class DatePickerDialogTest {

  @Test
  public void returnsTheInitialYearMonthAndDayPassedIntoTheDatePickerDialog() throws Exception {
    DatePickerDialog datePickerDialog = new DatePickerDialog(Robolectric.application, null, 2012, 6, 7);
    ShadowDatePickerDialog shadow = shadowOf(datePickerDialog);
    assertThat(shadow.getYear()).isEqualTo(2012);
    assertThat(shadow.getMonthOfYear()).isEqualTo(6);
    assertThat(shadow.getDayOfMonth()).isEqualTo(7);
  }
}
