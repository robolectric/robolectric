package com.xtremelabs.robolectric.shadows;

import android.widget.SeekBar;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SeekBar.class)
public class ShadowSeekBar extends ShadowAbsSeekBar {
	
	private SeekBar.OnSeekBarChangeListener listener;

    @Implementation
	public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener l) {
		listener = l;		
	}
    
    public SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
    	return listener;
    }
}
