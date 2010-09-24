package com.xtremelabs.droidsugar.fakes;

import android.graphics.Paint;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
public class FakePaint {

    public int color;
    public Paint.Style style;
    public Paint.Cap cap;
    public Paint.Join join;
    public float width;
    public float shadowRadius;
    public float shadowDx;
    public float shadowDy;
    public int shadowColor;

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setStyle(Paint.Style style) {
        this.style = style;
    }

    public Paint.Style getStyle() {
        return style;
    }

    public void setStrokeCap(Paint.Cap cap) {
        this.cap = cap;
    }

    public Paint.Cap getStrokeCap() {
        return cap;
    }

    public void setStrokeJoin(Paint.Join join) {
        this.join = join;
    }

    public Paint.Join getStrokeJoin() {
        return join;
    }

    public void setStrokeWidth(float width) {
        this.width = width;
    }

    public float getStrokeWidth() {
        return width;
    }

    public void setShadowLayer(float radius, float dx, float dy, int color) {
        shadowRadius = radius;
        shadowDx = dx;
        shadowDy = dy;
        shadowColor = color;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public float getShadowDx() {
        return shadowDx;
    }

    public float getShadowDy() {
        return shadowDy;
    }

    public int getShadowColor() {
        return shadowColor;
    }
}
