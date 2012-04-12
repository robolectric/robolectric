package com.xtremelabs.robolectric.shadows;

import android.os.Vibrator;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Vibrator.class)
public class ShadowVibrator {
    private boolean cancelled;
    private long milliseconds;
    private long[] pattern;
    private int repeat;
    
    @Implementation
    public void vibrate(long milliseconds) {
        this.milliseconds = milliseconds;
    }
    
    @Implementation
    public void vibrate(long[] pattern, int repeat) {
        this.pattern = pattern;
        this.repeat = repeat;
    }
    
    @Implementation
    public void cancel() {
        cancelled = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public long getMilliseconds() {
        return milliseconds;
    }
    
    public long[] getPattern() {
        return pattern;
    }
    
    public int getRepeat() {
        return repeat;
    }
}