package org.robolectric.shadows;

import android.view.animation.Animation;
import org.robolectric.annotation.Implements;

@Implements(Animation.class)
public class ShadowAnimation {
  private int loadedFromResourceId = -1;

  public void setLoadedFromResourceId(int loadedFromResourceId) {
    this.loadedFromResourceId = loadedFromResourceId;
  }

  public int getLoadedFromResourceId() {
    if (loadedFromResourceId == -1) {
      throw new IllegalStateException("Not loaded from a resource!");
    }
    return loadedFromResourceId;
  }
}