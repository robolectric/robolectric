package org.robolectric.shadows;

import android.app.TimePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ShadowThingy;

import static org.robolectric.util.ReflectionHelpers.ClassParameter;

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

    ShadowThingy.invokeConstructor(TimePickerDialog.class, realTimePickerDialog, new ClassParameter(Context.class, context),
        new ClassParameter(int.class, theme), new ClassParameter(TimePickerDialog.OnTimeSetListener.class, callBack),
        new ClassParameter(int.class, hourOfDay), new ClassParameter(int.class, minute), new ClassParameter(boolean.class, is24HourView));
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public int getMinute() {
    return minute;
  }
}
