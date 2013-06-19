package com.xtremelabs.robolectric.shadows;

import android.widget.AbsSeekBar;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(AbsSeekBar.class)
public class ShadowAbsSeekBar extends ShadowProgressBar {
    boolean mIsUserSeekable = true;
    private int thumbOffset;

    @Implementation
    public int getThumbOffset() {
        return thumbOffset;
    }

    @Implementation
    public void setThumbOffset(int thumbOffset) {
        this.thumbOffset = thumbOffset;
    }
}
