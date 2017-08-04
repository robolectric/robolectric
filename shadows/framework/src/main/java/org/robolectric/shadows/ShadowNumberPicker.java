package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.widget.NumberPicker;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(value = NumberPicker.class)
public class ShadowNumberPicker extends ShadowLinearLayout {
  @RealObject
  private NumberPicker realObject;
  private int value;
  private int minValue;
  private int maxValue;
  private boolean wrapSelectorWheel;
  private String[] displayedValues;
  private NumberPicker.OnValueChangeListener onValueChangeListener;

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
    this.displayedValues = displayedValues;
  }

  @Implementation
  public String[] getDisplayedValues() {
    return displayedValues;
  }

  @Implementation
  public void setMinValue(int minValue) {
    this.minValue = minValue;
  }

  @Implementation
  public void setMaxValue(int maxValue) {
    this.maxValue = maxValue;
  }

  @Implementation
  public int getMinValue() {
    return this.minValue;
  }

  @Implementation
  public int getMaxValue() {
    return this.maxValue;
  }

  @Implementation
  public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
    this.wrapSelectorWheel = wrapSelectorWheel;
  }

  @Implementation
  public boolean getWrapSelectorWheel() {
    return wrapSelectorWheel;
  }

  @Implementation
  public void setOnValueChangedListener(NumberPicker.OnValueChangeListener listener) {
    directlyOn(realObject, NumberPicker.class).setOnValueChangedListener(listener);
    this.onValueChangeListener = listener;
  }

  public NumberPicker.OnValueChangeListener getOnValueChangeListener() {
    return onValueChangeListener;
  }
}
