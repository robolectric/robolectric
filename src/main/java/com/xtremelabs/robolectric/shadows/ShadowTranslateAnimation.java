package com.xtremelabs.robolectric.shadows;

import android.view.animation.TranslateAnimation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TranslateAnimation.class)
public class ShadowTranslateAnimation extends ShadowAnimation {

    private int fromXType;
    private float fromXValue;
    private int toXType;
    private float toXValue;
    private int fromYType;
    private float fromYValue;
    private int toYType;
    private float toYValue;

    public void __constructor__(float fromXValue, float toXValue,
             float fromYValue, float toYValue) {
	    this.fromXType = TranslateAnimation.ABSOLUTE;
	    this.fromXValue = fromXValue;
	    this.toXType = TranslateAnimation.ABSOLUTE;
	    this.toXValue = toXValue;
	    this.fromYType = TranslateAnimation.ABSOLUTE;
	    this.fromYValue = fromYValue;
	    this.toYType = TranslateAnimation.ABSOLUTE;
	    this.toYValue = toYValue;
	}
    
    public void __constructor__(int fromXType, float fromXValue, int toXType, float toXValue,
                int fromYType, float fromYValue, int toYType, float toYValue) {
        this.fromXType = fromXType;
        this.fromXValue = fromXValue;
        this.toXType = toXType;
        this.toXValue = toXValue;
        this.fromYType = fromYType;
        this.fromYValue = fromYValue;
        this.toYType = toYType;
        this.toYValue = toYValue;
    }

    public int getFromXType() {
        return fromXType;
    }

    public float getFromXValue() {
        return fromXValue;
    }

    public int getToXType() {
        return toXType;
    }

    public float getToXValue() {
        return toXValue;
    }

    public int getFromYType() {
        return fromYType;
    }

    public float getFromYValue() {
        return fromYValue;
    }

    public int getToYType() {
        return toYType;
    }

    public float getToYValue() {
        return toYValue;
    }
}
