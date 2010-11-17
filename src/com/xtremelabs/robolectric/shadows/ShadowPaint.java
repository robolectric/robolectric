package com.xtremelabs.robolectric.shadows;

import android.graphics.Paint;
import android.graphics.Shader;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
public class ShadowPaint {

    private int color;
    private Paint.Style style;
    private Paint.Cap cap;
    private Paint.Join join;
    private float width;
    private float shadowRadius;
    private float shadowDx;
    private float shadowDy;
    private int shadowColor;
    private Shader shader;
    private int alpha;

    @Implementation
    public Shader setShader(Shader shader) {
        this.shader = shader;
        return shader;
    }

    @Implementation
    public int getAlpha() {
        return alpha;
    }

    @Implementation
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }


    @Implementation
    public Shader getShader() {
        return shader;
    }

    @Implementation
    public void setColor(int color) {
        this.color = color;
    }

    @Implementation
    public int getColor() {
        return color;
    }

    @Implementation
    public void setStyle(Paint.Style style) {
        this.style = style;
    }

    @Implementation
    public Paint.Style getStyle() {
        return style;
    }

    @Implementation
    public void setStrokeCap(Paint.Cap cap) {
        this.cap = cap;
    }

    @Implementation
    public Paint.Cap getStrokeCap() {
        return cap;
    }

    @Implementation
    public void setStrokeJoin(Paint.Join join) {
        this.join = join;
    }

    @Implementation
    public Paint.Join getStrokeJoin() {
        return join;
    }

    @Implementation
    public void setStrokeWidth(float width) {
        this.width = width;
    }

    @Implementation
    public float getStrokeWidth() {
        return width;
    }

    @Implementation
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

    public Paint.Cap getCap() {
        return cap;
    }

    public Paint.Join getJoin() {
        return join;
    }

    public float getWidth() {
        return width;
    }
}
