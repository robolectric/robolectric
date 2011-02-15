package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code ProgressBar} that has some extra accessors so that tests
 * can tell whether a {@code ProgressBar} object was created with the expected
 * parameters.
 *
 * @author Rob Dickerson (rc.dickerson@gmail.com)
 */
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
