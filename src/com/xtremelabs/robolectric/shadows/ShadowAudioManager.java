package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import android.media.AudioManager;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAudioManager {
	
	private int streamMaxVolume = 15;
	private int streamVolume = 7;

	private int streamType;
	private int flags;
	
	@Implementation
	public int getStreamMaxVolume(int streamType) {
		this.streamType = streamType;
		return streamMaxVolume;
	}
	
	@Implementation
	int getStreamVolume(int streamType) {
		this.streamType = streamType;
		return streamVolume;
	}
	
	@Implementation
	void setStreamVolume(int streamType, int index, int flags) {
		this.streamType = streamType;
		this.streamVolume = index;
		this.flags = flags;
	}

	public int getStreamMaxVolume() {
		return streamMaxVolume;
	}

	public void setStreamMaxVolume(int streamMaxVolume) {
		this.streamMaxVolume = streamMaxVolume;
	}

	public int getStreamVolume() {
		return streamVolume;
	}

	public void setStreamVolume(int streamVolume) {
		this.streamVolume = streamVolume;
	}

	public int getStreamType() {
		return streamType;
	}

	public void setStreamType(int streamType) {
		this.streamType = streamType;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}
	
}
