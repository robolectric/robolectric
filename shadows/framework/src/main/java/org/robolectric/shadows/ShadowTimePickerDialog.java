package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.TimePickerDialog;
import android.widget.TimePicker;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = TimePickerDialog.class)
public class ShadowTimePickerDialog extends ShadowAlertDialog {
  @RealObject
  protected TimePickerDialog realTimePickerDialog;

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
