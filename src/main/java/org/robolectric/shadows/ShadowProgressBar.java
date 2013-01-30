package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.widget.ProgressBar;

@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {

    private int progress;
    private int secondaryProgress;
    private int max = 100;
    private boolean isIndeterminate;

    @Override
    public void applyAttributes() {
        super.applyAttributes();

        final int max = attributeSet.getAttributeIntValue("android", "max", this.max);

        if (max >= 0)
            setMax(max);
    }

    @Implementation
    public void setMax(int max) {
        this.max = max;
        if (progress > max) {
            progress = max;
        }
    }

    @Implementation
    public int getMax() {
        return max;
    }

    @Implementation
    public void setProgress(int progress) {
        if (!isIndeterminate()) this.progress = Math.min(max, progress);
    }

    @Implementation
    public int getProgress() {
        return isIndeterminate ? 0 : progress;
    }

    @Implementation
    public void setSecondaryProgress(int secondaryProgress) {
        if (!isIndeterminate()) this.secondaryProgress = Math.min(max, secondaryProgress);
    }

    @Implementation
    public int getSecondaryProgress() {
        return isIndeterminate ? 0 : secondaryProgress;
    }

    @Implementation
    public void setIndeterminate(boolean indeterminate) {
        this.isIndeterminate = indeterminate;
    }

    @Implementation
    public boolean isIndeterminate() {
        return isIndeterminate;
    }

    @Implementation
    public void incrementProgressBy(int diff) {
        if (!isIndeterminate()) setProgress(progress + diff);
    }

    @Implementation
    public void incrementSecondaryProgressBy(int diff) {
        if (!isIndeterminate()) setSecondaryProgress(secondaryProgress + diff);
    }
}
