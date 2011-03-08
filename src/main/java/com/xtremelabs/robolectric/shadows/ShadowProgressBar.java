package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {

  private int progress;
  private int max;

  @Implementation
  public void setMax(int max) {
    progress = 0;
    this.max = max;
  }

  @Implementation
  public int getMax() {
    return max;
  }

  @Implementation
  public void setProgress(int progress) {
    this.progress = progress;
  }

  @Implementation
  public int getProgress() {
    return progress;
  }
}
