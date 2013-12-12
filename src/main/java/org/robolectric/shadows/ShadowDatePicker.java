package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.DatePicker;
import org.robolectric.annotation.Implements;

@Implements(value = DatePicker.class)
public class ShadowDatePicker extends ShadowFrameLayout {

  public void __constructor__(Context context, AttributeSet attrs, int defStyle) {
  }

}
