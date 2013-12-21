package org.robolectric.shadows;

import android.widget.NumberPicker;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;


@Implements(NumberPicker.class)
public class ShadowNumberPicker extends ShadowLinearLayout {
  private int value;
  private String[] displayedValues;
  private int minValue;
  private int maxValue;
  private boolean wrapSelectorWheel;

  @RealObject
  private NumberPicker realNumberPicker;

  @Implementation
  public void setValue(int value) {
    this.value = value;
  }

  @Implementation
  public int getValue() {
    return value;
  }

  @Implementation
  public void setDisplayedValues(String[] displayedValues) {
    if (displayedValues.length != (maxValue - minValue) + 1) {
      throw new RuntimeException("Displayed values should fit into range min and max values");
    }
    this.displayedValues = displayedValues;
  }

  @Implementation
  public String[] getDisplayedValues() {
    return displayedValues;
  }

  @Implementation
  public void setMinValue(int minValue) {
    this.minValue = minValue;
    Robolectric.directlyOn(realNumberPicker, NumberPicker.class, "setMinValue", Integer.TYPE).invoke(minValue);
  }

  @Implementation
  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
    Robolectric.directlyOn(realNumberPicker, NumberPicker.class, "setMaxValue", Integer.TYPE).invoke(maxValue);
  }

  @Implementation
  public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
    this.wrapSelectorWheel = wrapSelectorWheel;
  }

  @Implementation
  public boolean getWrapSelectorWheel() {
    return wrapSelectorWheel;
  }
}
