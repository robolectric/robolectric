package com.xtremelabs.robolectric.shadows;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ShapeDrawable.class)
public class ShadowShapeDrawable extends ShadowDrawable {
    
    private Paint paint = new Paint();

    @Implementation
    public android.graphics.Paint getPaint() {
        return paint;
    }
}
