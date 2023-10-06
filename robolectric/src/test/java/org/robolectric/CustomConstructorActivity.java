package org.robolectric;

import android.app.Activity;

public class CustomConstructorActivity extends Activity {
  private final int intValue;

  public CustomConstructorActivity(int intValue) {
    this.intValue = intValue;
  }

  public int getIntValue() {
    return intValue;
  }
}
