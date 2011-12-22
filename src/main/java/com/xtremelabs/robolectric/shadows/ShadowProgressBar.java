package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {

  private int progress;
  private int secondaryProgress;
  private int max = 100;

  @Override public void applyAttributes() {
      super.applyAttributes();
      
      final int max = attributeSet.getAttributeIntValue("android", "max", this.max);
      
      if (max >= 0)
          setMax(max);
  }
  
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
  
  @Implementation
  public void setSecondaryProgress(int secondaryProgress) {
    this.secondaryProgress = secondaryProgress;
  }
  
  @Implementation
  public int getSecondaryProgress() {
    return secondaryProgress;
  }
}
