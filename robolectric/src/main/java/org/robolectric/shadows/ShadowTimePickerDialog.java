package org.robolectric.shadows;

import android.app.TimePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;

@Implements(value = TimePickerDialog.class, inheritImplementationMethods = true)
public class ShadowTimePickerDialog extends ShadowAlertDialog {
  @RealObject
  protected TimePickerDialog realTimePickerDialog;
  private int hourOfDay;
  private int minute;

  public void __constructor__(Context context, int theme, TimePickerDialog.OnTimeSetListener callBack,
                              int hourOfDay, int minute, boolean is24HourView) {
    this.hourOfDay = hourOfDay;
    this.minute = minute;

    RobolectricInternals.getConstructor(TimePickerDialog.class, realTimePickerDialog, Context.class, int.class,
        TimePickerDialog.OnTimeSetListener.class, int.class, int.class, boolean.class)
        .invoke(context, theme, callBack, hourOfDay, minute, is24HourView);
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public int getMinute() {
    return minute;
  }
}
