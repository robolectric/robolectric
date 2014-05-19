package org.robolectric.shadows;

import android.app.DatePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;

@Implements(value = DatePickerDialog.class, inheritImplementationMethods = true)
public class ShadowDatePickerDialog extends ShadowAlertDialog {
  @RealObject
  protected DatePickerDialog realDatePickerDialog;
  private int year;
  private int monthOfYear;
  private int dayOfMonth;

  public void __constructor__(Context context, int theme, DatePickerDialog.OnDateSetListener callBack,
                              int year, int monthOfYear, int dayOfMonth) {
    this.year = year;
    this.monthOfYear = monthOfYear;
    this.dayOfMonth = dayOfMonth;

    RobolectricInternals.getConstructor(DatePickerDialog.class, realDatePickerDialog, Context.class, int.class,
        DatePickerDialog.OnDateSetListener.class, int.class, int.class, int.class)
        .invoke(context, theme, callBack, year, monthOfYear, dayOfMonth);
  }

  public int getYear() { return year; }

  public int getMonthOfYear() { return monthOfYear; }

  public int getDayOfMonth() { return dayOfMonth; }
}
