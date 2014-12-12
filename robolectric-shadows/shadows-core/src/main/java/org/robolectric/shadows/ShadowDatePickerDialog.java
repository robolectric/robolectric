package org.robolectric.shadows;

import android.app.DatePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.internal.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

@Implements(DatePickerDialog.class)
public class ShadowDatePickerDialog extends ShadowAlertDialog {

  @RealObject protected DatePickerDialog realDatePickerDialog;
  private int year;
  private int monthOfYear;
  private int dayOfMonth;

  public void __constructor__(Context context, int theme, DatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
    this.year = year;
    this.monthOfYear = monthOfYear;
    this.dayOfMonth = dayOfMonth;

    invokeConstructor(DatePickerDialog.class, realDatePickerDialog,
        from(Context.class, context),
        from(theme),
        from(DatePickerDialog.OnDateSetListener.class, callBack),
        from(year),
        from(monthOfYear),
        from(dayOfMonth));
  }

  public int getYear() {
    return year;
  }

  public int getMonthOfYear() {
    return monthOfYear;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }
}
