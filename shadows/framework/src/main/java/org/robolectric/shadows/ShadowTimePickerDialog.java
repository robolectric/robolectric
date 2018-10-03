package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.ClassParameter;

import android.app.TimePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(value = TimePickerDialog.class)
public class ShadowTimePickerDialog extends ShadowAlertDialog {
  @RealObject
  protected TimePickerDialog realTimePickerDialog;
  private int hourOfDay;
  private int minute;

  @Implementation
  protected void __constructor__(
      Context context,
      int theme,
      TimePickerDialog.OnTimeSetListener callBack,
      int hourOfDay,
      int minute,
      boolean is24HourView) {
    this.hourOfDay = hourOfDay;
    this.minute = minute;

    Shadow.invokeConstructor(TimePickerDialog.class, realTimePickerDialog,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(int.class, theme),
        ClassParameter.from(TimePickerDialog.OnTimeSetListener.class, callBack),
        ClassParameter.from(int.class, hourOfDay),
        ClassParameter.from(int.class, minute),
        ClassParameter.from(boolean.class, is24HourView));
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public int getMinute() {
    return minute;
  }
}
