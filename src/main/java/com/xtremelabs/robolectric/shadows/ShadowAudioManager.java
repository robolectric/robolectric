package com.xtremelabs.robolectric.shadows;

import android.media.AudioManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAudioManager {

    private int streamMaxVolume = 15;
    private int streamVolume = 7;
    private int flags;
    private AudioFocusRequest lastAudioFocusRequest;
    private int nextResponseValue = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    private AudioManager.OnAudioFocusChangeListener lastAbandonedAudioFocusListener;

    @Implementation
    public int getStreamMaxVolume(int streamType) {
        return streamMaxVolume;
    }

    @Implementation
    public int getStreamVolume(int streamType) {
        return streamVolume;
    }

    @Implementation
    public void setStreamVolume(int streamType, int index, int flags) {
        this.streamVolume = index;
        this.flags = flags;
    }

    @Implementation
    public int requestAudioFocus(android.media.AudioManager.OnAudioFocusChangeListener l, int streamType, int durationHint) {
        lastAudioFocusRequest = new AudioFocusRequest(l, streamType, durationHint);
        return nextResponseValue;
    }

    @Implementation
    public int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener l) {
        lastAbandonedAudioFocusListener = l;
        return nextResponseValue;
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

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public AudioFocusRequest getLastAudioFocusRequest() {
        return lastAudioFocusRequest;
    }

    public void setNextFocusRequestResponse(int nextResponseValue) {
        this.nextResponseValue = nextResponseValue;
    }

    public AudioManager.OnAudioFocusChangeListener getLastAbandonedAudioFocusListener() {
        return lastAbandonedAudioFocusListener;
    }

    public static class AudioFocusRequest {
        public final AudioManager.OnAudioFocusChangeListener listener;
        public final int streamType;
        public final int durationHint;

        private AudioFocusRequest(AudioManager.OnAudioFocusChangeListener listener, int streamType, int durationHint) {
            this.listener = listener;
            this.streamType = streamType;
            this.durationHint = durationHint;
        }
    }
}
