package org.robolectric.shadows;

import android.widget.SeekBar;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;

@Implements(SeekBar.class)
public class ShadowSeekBar extends ShadowView {

  private SeekBar.OnSeekBarChangeListener listener;

  @Filter
  protected void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
    this.listener = listener;
  }

  public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
    return this.listener;
  }
}
