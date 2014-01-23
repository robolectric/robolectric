package org.robolectric.shadows;

import android.app.TimePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;

@Implements(value = TimePickerDialog.class, inheritImplementationMethods = true)
public class ShadowTimePickerDialog extends ShadowAlertDialog {
  public void __constructor__(Context context, int theme, TimePickerDialog.OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
  }
}
