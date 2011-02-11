package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {
	
	private int max;
	private int progress;

	@Implementation
	public synchronized int getMax() {
		return max;
	}
	
	@Implementation
	public synchronized int getProgress() {
		return progress;
	}
	
	@Implementation
	public synchronized void setMax(int max) {
		this.max = max;
	}
	
	@Implementation
	public synchronized void setProgress(int progress) {
		this.progress = progress;
	}
}
