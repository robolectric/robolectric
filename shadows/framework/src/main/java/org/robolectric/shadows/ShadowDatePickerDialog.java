package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.DatePickerDialog;
import android.content.Context;
import java.util.Calendar;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(DatePickerDialog.class)
public class ShadowDatePickerDialog extends ShadowAlertDialog {

  @RealObject protected DatePickerDialog realDatePickerDialog;
  private Calendar calendar;
  private int year;
  private int monthOfYear;
  private int dayOfMonth;
  private DatePickerDialog.OnDateSetListener callBack;

  @Implementation(maxSdk = M)
  protected void __constructor__(
      Context context,
      int theme,
      DatePickerDialog.OnDateSetListener callBack,
      int year,
      int monthOfYear,
      int dayOfMonth) {
    this.year = year;
    this.monthOfYear = monthOfYear;
    this.dayOfMonth = dayOfMonth;
    this.callBack = callBack;

    invokeConstructor(DatePickerDialog.class, realDatePickerDialog,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(int.class, theme),
        ClassParameter.from(DatePickerDialog.OnDateSetListener.class, callBack),
        ClassParameter.from(int.class, year),
        ClassParameter.from(int.class, monthOfYear),
        ClassParameter.from(int.class, dayOfMonth));
  }

  @Implementation(minSdk = N)
  protected void __constructor__(
      Context context,
      int theme,
      DatePickerDialog.OnDateSetListener callBack,
      Calendar calendar,
      int year,
      int monthOfYear,
      int dayOfMonth) {
    this.calendar = calendar;
    this.year = year;
    this.monthOfYear = monthOfYear;
    this.dayOfMonth = dayOfMonth;
    this.callBack = callBack;

    invokeConstructor(DatePickerDialog.class, realDatePickerDialog,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(int.class, theme),
        ClassParameter.from(DatePickerDialog.OnDateSetListener.class, callBack),
        ClassParameter.from(Calendar.class, calendar),
        ClassParameter.from(int.class, year),
        ClassParameter.from(int.class, monthOfYear),
        ClassParameter.from(int.class, dayOfMonth));
  }

  public Calendar getCalendar() {
    return calendar;
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

  public DatePickerDialog.OnDateSetListener getOnDateSetListenerCallback() {
    return this.callBack;
  }
}
