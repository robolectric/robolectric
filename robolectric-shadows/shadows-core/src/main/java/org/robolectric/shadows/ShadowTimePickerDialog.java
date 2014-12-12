package org.robolectric.shadows;

import android.app.TimePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.Shadow;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

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

    Shadow.invokeConstructor(TimePickerDialog.class, realTimePickerDialog,
        from(Context.class, context),
        from(theme),
        from(TimePickerDialog.OnTimeSetListener.class, callBack),
        from(hourOfDay),
        from(minute),
        from(is24HourView));
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public int getMinute() {
    return minute;
  }
}
