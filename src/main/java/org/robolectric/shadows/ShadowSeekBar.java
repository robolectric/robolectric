package com.xtremelabs.robolectric.shadows;

import android.widget.SeekBar;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(SeekBar.class)
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
    	if(listener != null) {
    		listener.onProgressChanged( realSeekBar, progress, true);
    	}    	
    }
    
    public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
    	return this.listener;
    }
}
