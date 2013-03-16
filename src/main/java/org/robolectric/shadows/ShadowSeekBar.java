package org.robolectric.shadows;

import android.widget.SeekBar;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(value = SeekBar.class, inheritImplementationMethods = true)
public class ShadowSeekBar extends ShadowAbsSeekBar {

    @RealObject
    private SeekBar realSeekBar;

    private SeekBar.OnSeekBarChangeListener listener;

    @Implementation
    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    @Override
    @Implementation
    public void setProgress(int progress) {
        super.setProgress(progress);
        if (listener != null) {
            listener.onProgressChanged(realSeekBar, progress, true);
        }
    }

    public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return this.listener;
    }
}
