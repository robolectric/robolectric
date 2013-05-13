package org.robolectric.shadows;

import android.content.pm.ActivityInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ActivityInfo.class)
public class ShadowActivityInfo {

  @RealObject
  private ActivityInfo realInfo;

  @Implementation
  public String toString() {
    return realInfo.name;
  }

}
