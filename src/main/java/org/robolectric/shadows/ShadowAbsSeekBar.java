package com.xtremelabs.robolectric.shadows;

import android.widget.AbsSeekBar;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(AbsSeekBar.class)
public class ShadowAbsSeekBar extends ShadowProgressBar {
    
    boolean mIsUserSeekable = true;
    
}
