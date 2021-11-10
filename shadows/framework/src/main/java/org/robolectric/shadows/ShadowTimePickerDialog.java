package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = TimePickerDialog.class)
public class ShadowTimePickerDialog extends ShadowAlertDialog {
  @RealObject
  protected TimePickerDialog realTimePickerDialog;

  @Implementation
  protected void __constructor__(
      Context context,
      int theme,
      TimePickerDialog.OnTimeSetListener callBack,
      int hourOfDay,
      int minute,
      boolean is24HourView) {

    Shadow.invokeConstructor(TimePickerDialog.class, realTimePickerDialog,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(int.class, theme),
        ClassParameter.from(TimePickerDialog.OnTimeSetListener.class, callBack),
        ClassParameter.from(int.class, hourOfDay),
        ClassParameter.from(int.class, minute),
        ClassParameter.from(boolean.class, is24HourView));
  }

  public int getHourOfDay() {
    return reflector(TimePickerDialogProvider.class, realTimePickerDialog)
        .getTimePicker()
        .getCurrentHour();
  }

  public int getMinute() {
    return reflector(TimePickerDialogProvider.class, realTimePickerDialog)
        .getTimePicker()
        .getCurrentMinute();
  }

  public boolean getIs24HourView() {
    return reflector(TimePickerDialogProvider.class, realTimePickerDialog)
        .getTimePicker()
        .is24HourView();
  }

  @ForType(TimePickerDialog.class)
  interface TimePickerDialogProvider {

    @Accessor("mTimePicker")
    TimePicker getTimePicker();
  }
}
