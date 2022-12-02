package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.NumberPicker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(value = NumberPicker.class)
public class ShadowNumberPicker extends ShadowLinearLayout {
  @RealObject private NumberPicker realNumberPicker;
  private String[] displayedValues;
  private NumberPicker.OnValueChangeListener onValueChangeListener;

  @Implementation
  protected void setDisplayedValues(String[] displayedValues) {
    if (ShadowView.useRealGraphics()) {
      reflector(NumberPickerReflector.class, realNumberPicker).setDisplayedValues(displayedValues);
    } else {
      this.displayedValues = displayedValues;
    }
  }

  @Implementation
  protected String[] getDisplayedValues() {
    if (ShadowView.useRealGraphics()) {
      return reflector(NumberPickerReflector.class, realNumberPicker).getDisplayedValues();
    } else {
      return displayedValues;
    }
  }

  @Implementation
  protected void setOnValueChangedListener(NumberPicker.OnValueChangeListener listener) {
    reflector(NumberPickerReflector.class, realNumberPicker).setOnValueChangedListener(listener);
    this.onValueChangeListener = listener;
  }

  public NumberPicker.OnValueChangeListener getOnValueChangeListener() {
    return onValueChangeListener;
  }

  @ForType(NumberPicker.class)
  interface NumberPickerReflector {

    @Direct
    void setOnValueChangedListener(NumberPicker.OnValueChangeListener listener);

    @Direct
    void setDisplayedValues(String[] displayedValues);

    @Direct
    String[] getDisplayedValues();
  }
}
