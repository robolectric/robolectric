package org.robolectric.shadows;

import android.widget.AbsSeekBar;
import org.robolectric.annotation.Implements;

@Implements(value = AbsSeekBar.class)
public class ShadowAbsSeekBar extends ShadowProgressBar {

  boolean mIsUserSeekable = true;

}
