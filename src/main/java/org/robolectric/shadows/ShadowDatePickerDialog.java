package org.robolectric.shadows;

import android.app.DatePickerDialog;
import android.content.Context;
import org.robolectric.annotation.Implements;

@Implements(value = DatePickerDialog.class, inheritImplementationMethods = true)
public class ShadowDatePickerDialog extends ShadowAlertDialog {
  public void __constructor__(Context context, int theme, DatePickerDialog.OnDateSetListener callBack,
                              int year, int monthOfYear, int dayOfMonth) {
  }
}
