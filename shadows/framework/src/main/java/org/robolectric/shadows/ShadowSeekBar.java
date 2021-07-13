package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.SeekBar;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(SeekBar.class)
public class ShadowSeekBar extends ShadowAbsSeekBar {

  @RealObject private SeekBar realSeekBar;

  private SeekBar.OnSeekBarChangeListener listener;

  @Implementation
  protected void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
    this.listener = listener;
    reflector(SeekBarReflector.class, realSeekBar).setOnSeekBarChangeListener(listener);
  }

  public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
    return this.listener;
  }

  @ForType(SeekBar.class)
  interface SeekBarReflector {

    @Direct
    void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener);
  }
}
