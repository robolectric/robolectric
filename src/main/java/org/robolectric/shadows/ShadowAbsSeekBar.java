package org.robolectric.shadows;

import android.widget.AbsSeekBar;

import org.robolectric.internal.Implements;

@Implements(AbsSeekBar.class)
public class ShadowAbsSeekBar extends ShadowProgressBar {
    
    boolean mIsUserSeekable = true;
    
}
