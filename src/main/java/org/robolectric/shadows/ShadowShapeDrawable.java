package org.robolectric.shadows;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(ShapeDrawable.class)
public class ShadowShapeDrawable extends ShadowDrawable {
    
    private Paint paint = new Paint();

    @Implementation
    public android.graphics.Paint getPaint() {
        return paint;
    }
}
