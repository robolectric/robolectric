package org.robolectric.shadows;

import android.text.format.Time;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Time.class)
public class ShadowTime {
  @RealObject
  private Time time;

  @Implementation
  public void setToNow() {
    time.set(ShadowSystemClock.currentTimeMillis());
  }
}
