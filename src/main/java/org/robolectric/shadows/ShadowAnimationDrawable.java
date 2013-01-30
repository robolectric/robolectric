package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@Implements(AnimationDrawable.class)
public class ShadowAnimationDrawable extends ShadowDrawable {

    private List<Drawable> frames = new ArrayList<Drawable>();
    private List<Integer> durations = new ArrayList<Integer>();
    private boolean isStarted;

    @Implementation
    public void addFrame(Drawable frame, int duration) {
        frames.add(frame);
        durations.add(duration);
    }

    @Implementation
    public int getNumberOfFrames() {
        return frames.size();
    }

    @Implementation
    public int getDuration(int i) {
        return durations.get(i);
    }

    @Implementation
    public void start() {
        isStarted = true;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public List<Drawable> getFrames() {
        return frames;
    }
}
