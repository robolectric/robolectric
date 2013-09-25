package org.robolectric.shadows;

import android.widget.SeekBar;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(SeekBar.class)
public class ShadowSeekBar extends ShadowAbsSeekBar {

  @RealObject
  private SeekBar realSeekBar;

  private SeekBar.OnSeekBarChangeListener listener;

  @Implementation
  public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
    this.listener = listener;
    Robolectric.directlyOn(realSeekBar, SeekBar.class).setOnSeekBarChangeListener(listener);
  }

  public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
    return this.listener;
  }
}
