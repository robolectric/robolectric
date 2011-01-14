package com.xtremelabs.robolectric.shadows;

import android.media.MediaPlayer;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(MediaPlayer.class)
public class ShadowMediaPlayer {

    private int currentPosition;

    @Implementation
    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        currentPosition = position;
    }
}
