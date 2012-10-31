package com.xtremelabs.robolectric.shadows;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LinearGradient.class)
public class ShadowLinearGradient {
    private float x0;
    private float y0;
    private float x1;
    private float y1;
    private int color0;
    private int color1;
    private Shader.TileMode tile;

    public void __constructor__(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.color0 = color0;
        this.color1 = color1;
        this.tile = tile;
    }

    public float getX0() {
        return x0;
    }

    public float getY0() {
        return y0;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public int getColor0() {
        return color0;
    }

    public int getColor1() {
        return color1;
    }

    public Shader.TileMode getTile() {
        return tile;
    }
}
