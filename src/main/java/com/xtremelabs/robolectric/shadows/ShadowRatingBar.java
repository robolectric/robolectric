package com.xtremelabs.robolectric.shadows;

import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(RatingBar.class)
public class ShadowRatingBar extends ShadowAbsSeekBar {

    @RealObject
    private RatingBar realRatingBar;
    private int mNumStars = 5;
    private OnRatingBarChangeListener listener;
    
    @Override public void applyAttributes() {
        super.applyAttributes();
        
        setIsIndicator(attributeSet.getAttributeBooleanValue("android", "isIndicator", false));
        final int numStars = attributeSet.getAttributeIntValue("android", "numStars", mNumStars);
        final float rating = attributeSet.getAttributeFloatValue("android", "rating", -1);
        final float stepSize = attributeSet.getAttributeFloatValue("android", "stepSize", -1);
        
        if (numStars > 0 && numStars != mNumStars) {
            setNumStars(numStars);            
        }
        
        if (stepSize >= 0) {
            setStepSize(stepSize);
        } else {
            setStepSize(0.5f);
        }
        
        if (rating >= 0) {
            setRating(rating);
        }
    }
    
    @Implementation
    public void setNumStars(final int numStars) {
        if (numStars <= 0) {
            return;
        }

        mNumStars = numStars;
    }

    @Implementation
    public int getNumStars() {
        return mNumStars;
    }

    @Implementation
    public void setRating(float rating) {
        setProgress(Math.round(rating * getProgressPerStar()));
    }

    @Implementation
    public float getRating() {
        return getProgress() / getProgressPerStar();
    }
    
    @Implementation
    public void setIsIndicator(boolean isIndicator) {
        mIsUserSeekable = !isIndicator;
        setFocusable(!isIndicator);
    }
    
    @Implementation
    public boolean isIndicator() {
        return !mIsUserSeekable;
    }
    
    @Implementation
    public void setStepSize(float stepSize) {
        if (stepSize <= 0) {
            return;
        }
        
        final float newMax = mNumStars / stepSize;
        final int newProgress = (int) (newMax / getMax() * getProgress());
        setMax((int) newMax);
        setProgress(newProgress);
    }
    
    @Implementation
    public float getStepSize() {
        return (float) getNumStars() / getMax();
    }
    
    private float getProgressPerStar() {
        if (mNumStars > 0) {
            return 1f * getMax() / mNumStars;
        } else {
            return 1;
        }
    }
    
    @Implementation
    @Override
    public void setProgress(int progress) {
        super.setProgress(progress);
        if (listener != null)
            listener.onRatingChanged(realRatingBar, getRating(), true);
    }
    
    @Implementation
    public void setOnRatingBarChangeListener(OnRatingBarChangeListener listener) {
        this.listener = listener;
    }

    @Implementation
    public OnRatingBarChangeListener getOnRatingBarChangeListener() {
        return listener;
    }
}
