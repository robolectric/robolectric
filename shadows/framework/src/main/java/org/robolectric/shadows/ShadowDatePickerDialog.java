package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import java.util.Calendar;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(DatePickerDialog.class)
public class ShadowDatePickerDialog extends ShadowAlertDialog {

  @RealObject protected DatePickerDialog realDatePickerDialog;
  private Calendar calendar;

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

    invokeConstructor(
        DatePickerDialog.class,
        realDatePickerDialog,
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
    return realDatePickerDialog.getDatePicker().getYear();
  }

  public int getMonthOfYear() {
    return realDatePickerDialog.getDatePicker().getMonth();
  }

  public int getDayOfMonth() {
    return realDatePickerDialog.getDatePicker().getDayOfMonth();
  }

  public DatePickerDialog.OnDateSetListener getOnDateSetListenerCallback() {

    return reflector(DatePickerDialogReflector.class, realDatePickerDialog).getDateSetListener();
  }

  @ForType(DatePickerDialog.class)
  interface DatePickerDialogReflector {

    @Accessor("mDateSetListener")
    OnDateSetListener getDateSetListener();
  }
}
